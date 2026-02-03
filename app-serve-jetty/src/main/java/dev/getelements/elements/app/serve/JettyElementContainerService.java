package dev.getelements.elements.app.serve;

import dev.getelements.elements.app.serve.loader.Loader;
import dev.getelements.elements.common.app.ElementContainerService;
import dev.getelements.elements.common.app.ElementRuntimeService;
import dev.getelements.elements.common.app.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.util.Monitor;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.common.app.ElementContainerService.ContainerStatus.*;

/**
 * Jetty implementation of {@link ElementContainerService} that polls the {@link ElementRuntimeService}
 * for active Element deployments and mounts them to the Jetty container using {@link Loader} implementations.
 */
public class JettyElementContainerService implements ElementContainerService {

    private static final Logger logger = LoggerFactory.getLogger(JettyElementContainerService.class);

    @ElementDefaultAttribute(value = "10", description = "Poll interval in seconds for checking runtime changes.")
    public static final String POLL_INTERVAL_SECONDS = "dev.getelements.elements.container.poll.interval.seconds";

    private final Lock lock = new ReentrantLock();

    private final Map<String, ActiveContainer> activeContainers = new HashMap<>();

    private ElementRuntimeService elementRuntimeService;

    private Set<Loader> loaders;

    private int pollIntervalSeconds;

    private ScheduledExecutorService scheduler;

    @Override
    public void start() {
        try (var mon = Monitor.enter(lock)) {

            if (scheduler != null) {
                throw new IllegalStateException("Already started");
            }

            // Create scheduler with daemon thread
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                final var thread = new Thread(r, "element-container-service");
                thread.setDaemon(true);
                return thread;
            });

            // Run initial sync immediately
            scheduler.execute(this::safeSync);

            // Schedule periodic synchronization
            scheduler.scheduleAtFixedRate(
                    this::safeSync,
                    pollIntervalSeconds,
                    pollIntervalSeconds,
                    TimeUnit.SECONDS
            );

            logger.info("JettyElementContainerService started with poll interval of {} seconds", pollIntervalSeconds);

        }
    }

    @Override
    public void stop() {
        try (var mon = Monitor.enter(lock)) {
            if (scheduler == null) {
                logger.warn("JettyElementContainerService not started");
                return;
            }

            logger.info("Stopping JettyElementContainerService...");

            // Shutdown scheduler
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException ex) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;

            // Unmount all active containers
            final var deploymentIds = new ArrayList<>(activeContainers.keySet());
            for (final String deploymentId : deploymentIds) {
                try {
                    doUnmount(deploymentId);
                } catch (Exception ex) {
                    logger.error("Error unmounting deployment {} during shutdown", deploymentId, ex);
                }
            }

            logger.info("JettyElementContainerService stopped");
        }
    }

    @Override
    public List<ContainerRecord> getActiveContainers() {
        try (var mon = Monitor.enter(lock)) {
            return activeContainers.values()
                    .stream()
                    .map(active -> new ContainerRecord(
                            active.runtime(),
                            active.status(),
                            active.uris(),
                            active.logs(),
                            active.errors()
                    ))
                    .toList();
        }
    }

    /**
     * Safe wrapper around sync that catches all exceptions.
     */
    private void safeSync() {
        try {
            sync();
        } catch (Exception ex) {
            logger.error("Error during container synchronization", ex);
        }
    }

    /**
     * Synchronizes container state with runtime state.
     */
    private void sync() {
        try (var mon = Monitor.enter(lock)) {

            logger.debug("Starting container synchronization...");

            // Fetch all active runtimes
            final List<RuntimeRecord> activeRuntimes;

            try {
                activeRuntimes = getElementRuntimeService().getActiveDeployments();
            } catch (Exception ex) {
                logger.error("Failed to fetch active runtimes", ex);
                return;
            }

            // Build set of deployment IDs from runtimes
            final var runtimeDeploymentIds = new HashSet<String>();
            for (final var runtime : activeRuntimes) {
                runtimeDeploymentIds.add(runtime.deployment().id());
            }

            // Process each runtime
            for (final var runtime : activeRuntimes) {
                final var deploymentId = runtime.deployment().id();

                // Skip failed runtimes
                if (runtime.status() == ElementRuntimeService.RuntimeStatus.FAILED) {
                    logger.debug("Skipping failed runtime: {}", deploymentId);
                    continue;
                }

                final var active = activeContainers.get(deploymentId);

                if (active == null) {
                    // New runtime - mount it
                    try {
                        doMount(runtime);
                        logger.info("Mounted deployment: {}", deploymentId);
                    } catch (Exception ex) {
                        logger.error("Failed to mount deployment: {}", deploymentId, ex);
                    }
                } else if (active.runtime().deployment().version() != runtime.deployment().version()) {
                    // Version changed - remount
                    try {
                        doUnmount(deploymentId);
                        doMount(runtime);
                        logger.info("Remounted deployment: {} (version {} -> {})",
                                deploymentId,
                                active.runtime().deployment().version(),
                                runtime.deployment().version());
                    } catch (Exception ex) {
                        logger.error("Failed to remount deployment: {}", deploymentId, ex);
                    }
                } else {
                    logger.debug("Deployment {} does not need remounting", deploymentId);
                }
            }

            // Unmount containers no longer in runtime
            final var activeIds = new ArrayList<>(activeContainers.keySet());
            for (final String activeId : activeIds) {
                if (!runtimeDeploymentIds.contains(activeId)) {
                    try {
                        doUnmount(activeId);
                        logger.info("Unmounted deployment: {}", activeId);
                    } catch (Exception ex) {
                        logger.error("Failed to unmount deployment: {}", activeId, ex);
                    }
                }
            }

            logger.debug("Container synchronization complete. Active containers: {}", activeContainers.size());
        }
    }

    /**
     * Mounts a runtime to the container.
     */
    private void doMount(final RuntimeRecord runtime) {
        final var deploymentId = runtime.deployment().id();
        final var uris = new TreeSet<URI>();
        final var logs = new ArrayList<String>();
        final var warnings = new ArrayList<String>();
        final var errors = new ArrayList<Throwable>();

        logs.add("Starting container mount for deployment " + deploymentId);

        try {
            // Create pending deployment context
            final var pending = new Loader.PendingDeployment(
                    uris::add,
                    logs::add,
                    warnings::add,
                    errors::add
            );

            // Run all loaders
            getLoaders().forEach(loader -> {
                try {
                    loader.load(pending, runtime);
                } catch (Throwable th) {
                    pending.error(th);
                }
            });

            // Determine status
            ContainerStatus status = CLEAN;
            if (!warnings.isEmpty()) {
                status = WARNINGS;
            }
            if (!errors.isEmpty()) {
                status = UNSTABLE;
            }

            logs.add("Container mount completed with status: " + status);

            // Track active container
            final var active = new ActiveContainer(
                    runtime,
                    status,
                    Set.copyOf(uris),
                    logs,
                    errors
            );

            activeContainers.put(deploymentId, active);

        } catch (Exception ex) {
            logs.add("Container mount failed: " + ex.getMessage());
            errors.add(ex);

            // Store failed container
            final var failedContainer = new ActiveContainer(
                    runtime,
                    FAILED,
                    Set.of(),
                    logs,
                    errors
            );

            activeContainers.put(deploymentId, failedContainer);
        }
    }

    /**
     * Unmounts a container.
     */
    private void doUnmount(final String deploymentId) {
        final var active = activeContainers.remove(deploymentId);
        if (active != null) {
            // TODO: Actually unregister handlers from Jetty
            // This will require changes to Loader interface to support cleanup
            logger.info("Unmounted container for deployment {}", deploymentId);
        }
    }

    public ElementRuntimeService getElementRuntimeService() {
        return elementRuntimeService;
    }

    @Inject
    public void setElementRuntimeService(final ElementRuntimeService elementRuntimeService) {
        this.elementRuntimeService = elementRuntimeService;
    }

    public Set<Loader> getLoaders() {
        return loaders;
    }

    @Inject
    public void setLoaders(final Set<Loader> loaders) {
        this.loaders = loaders;
    }

    public int getPollIntervalSeconds() {
        return pollIntervalSeconds;
    }

    @Inject
    public void setPollIntervalSeconds(@Named(POLL_INTERVAL_SECONDS) final int pollIntervalSeconds) {
        this.pollIntervalSeconds = pollIntervalSeconds;
    }

    /**
     * Tracks an active container's state.
     */
    private record ActiveContainer(
            RuntimeRecord runtime,
            ContainerStatus status,
            Set<URI> uris,
            List<String> logs,
            List<Throwable> errors
    ) {
        ActiveContainer {
            uris = uris != null ? Set.copyOf(uris) : Set.of();
            logs = logs != null ? List.copyOf(logs) : List.of();
            errors = errors != null ? List.copyOf(errors) : List.of();
        }
    }

}

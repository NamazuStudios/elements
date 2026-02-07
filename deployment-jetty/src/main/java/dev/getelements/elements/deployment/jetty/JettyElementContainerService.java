package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.deployment.jetty.loader.Loader;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.deployment.ElementContainerService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
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

import static dev.getelements.elements.sdk.deployment.ElementContainerService.ContainerStatus.*;

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

    private Set<Loader> loaders;

    private int pollIntervalSeconds;

    private ScheduledExecutorService scheduler;

    private ElementRuntimeService elementRuntimeService;

    private ElementRegistry rootElementRegistry;

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
                            active.errors(),
                            active.elements()
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
     * Event consumer for RuntimeLoaded events from ElementRuntimeService.
     * Immediately mounts the container when a runtime is loaded.
     */
    @ElementEventConsumer(ElementRuntimeService.RUNTIME_LOADED)
    public void onRuntimeLoaded(final String deploymentId,
                                final ElementRuntimeService.RuntimeStatus status,
                                final boolean isTransient,
                                final RuntimeRecord record) {
        ContainerRecord mountedRecord = null;

        try (var mon = Monitor.enter(lock)) {
            logger.info("Received RuntimeLoaded event for deployment: {}", deploymentId);

            // Skip failed runtimes
            if (status == ElementRuntimeService.RuntimeStatus.FAILED) {
                logger.debug("Skipping failed runtime: {}", deploymentId);
                return;
            }

            // Check if already mounted
            final var existing = activeContainers.get(deploymentId);
            if (existing != null) {
                logger.warn("Container already mounted for deployment: {}, remounting", deploymentId);
                doUnmount(deploymentId);
            }

            // Mount the new runtime
            try {
                doMount(record);
                logger.info("Mounted container in response to RuntimeLoaded event: {}", deploymentId);

                // Capture the mounted container for event publishing
                final var active = activeContainers.get(deploymentId);
                if (active != null) {
                    mountedRecord = new ContainerRecord(
                            active.runtime(),
                            active.status(),
                            active.uris(),
                            active.logs(),
                            active.errors(),
                            active.elements()
                    );
                }
            } catch (Exception ex) {
                logger.error("Failed to mount container for deployment: {}", deploymentId, ex);
            }
        }

        // Publish event OUTSIDE the lock
        if (mountedRecord != null) {
            publishContainerMounted(mountedRecord);
        }
    }

    /**
     * Event consumer for RuntimeUnloaded events from ElementRuntimeService.
     * Immediately unmounts the container when a runtime is unloaded.
     */
    @ElementEventConsumer(ElementRuntimeService.RUNTIME_UNLOADED)
    public void onRuntimeUnloaded(final String deploymentId) {
        boolean unmounted = false;

        try (var mon = Monitor.enter(lock)) {
            logger.info("Received RuntimeUnloaded event for deployment: {}", deploymentId);

            // Unmount the container if it exists
            final var existing = activeContainers.get(deploymentId);
            if (existing != null) {
                try {
                    doUnmount(deploymentId);
                    logger.info("Unmounted container in response to RuntimeUnloaded event: {}", deploymentId);
                    unmounted = true;
                } catch (Exception ex) {
                    logger.error("Failed to unmount container for deployment: {}", deploymentId, ex);
                }
            } else {
                logger.debug("No container found for unloaded deployment: {}", deploymentId);
            }
        }

        // Publish event OUTSIDE the lock
        if (unmounted) {
            publishContainerUnmounted(deploymentId);
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
                activeRuntimes = getElementRuntimeService().getActiveRuntimes();
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
        final var elements = new ArrayList<Element>();

        logs.add("Starting container mount for deployment " + deploymentId);

        try {
            // Create pending deployment context
            final var pending = new Loader.PendingDeployment(
                    uris::add,
                    logs::add,
                    warnings::add,
                    errors::add,
                    elements::add
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
                    errors,
                    elements
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
                    errors,
                    elements
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

            // Unload handlers from all loaders
            for (final Loader loader : getLoaders()) {
                try {
                    loader.unload(active.runtime());
                } catch (Exception ex) {
                    logger.error("Error unloading handlers for deployment {} from loader {}",
                            deploymentId,
                            loader.getClass().getSimpleName(),
                            ex
                    );
                }
            }

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

    public ElementRegistry getRootElementRegistry() {
        return rootElementRegistry;
    }

    @Inject
    public void setRootElementRegistry(@Named(dev.getelements.elements.sdk.ElementRegistry.ROOT) final ElementRegistry rootElementRegistry) {
        this.rootElementRegistry = rootElementRegistry;
    }

    /**
     * Publishes ContainerMounted event with the container record.
     * NOTE: Should be called OUTSIDE the lock to prevent double-locking.
     */
    private void publishContainerMounted(final ContainerRecord record) {
        try {
            final var event = Event.builder()
                    .named(ElementContainerService.CONTAINER_MOUNTED)
                    .argument(record.runtime().deployment().id())
                    .argument(record.status())
                    .argument(record)
                    .build();
            getRootElementRegistry().publish(event);
            logger.debug("Published container mounted event for deployment: {}",
                    record.runtime().deployment().id());
        } catch (Exception ex) {
            logger.error("Failed to publish container mounted event for deployment: {}",
                    record.runtime().deployment().id(), ex);
        }
    }

    /**
     * Publishes ContainerUnmounted event with the deployment ID.
     * NOTE: Should be called OUTSIDE the lock to prevent double-locking.
     */
    private void publishContainerUnmounted(final String deploymentId) {
        try {
            final var event = Event.builder()
                    .named(ElementContainerService.CONTAINER_UNMOUNTED)
                    .argument(deploymentId)
                    .build();
            getRootElementRegistry().publish(event);
            logger.debug("Published container unmounted event for deployment: {}", deploymentId);
        } catch (Exception ex) {
            logger.error("Failed to publish container unmounted event for deployment: {}", deploymentId, ex);
        }
    }

    /**
     * Tracks an active container's state.
     */
    private record ActiveContainer(
            RuntimeRecord runtime,
            ContainerStatus status,
            Set<URI> uris,
            List<String> logs,
            List<Throwable> errors,
            List<Element> elements
    ) {
        ActiveContainer {
            uris = uris != null ? Set.copyOf(uris) : Set.of();
            logs = logs != null ? List.copyOf(logs) : List.of();
            errors = errors != null ? List.copyOf(errors) : List.of();
            elements = elements != null ? List.copyOf(elements) : List.of();
        }
    }

}

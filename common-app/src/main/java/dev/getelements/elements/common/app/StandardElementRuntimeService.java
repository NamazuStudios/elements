package dev.getelements.elements.common.app;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectState;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

/**
 * Standard implementation of {@link ElementRuntimeService} that polls the database for
 * {@link ElementDeployment} records and manages their runtime lifecycle.
 */
public class StandardElementRuntimeService implements ElementRuntimeService {

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 90;

    private static final Logger logger = LoggerFactory.getLogger(StandardElementRuntimeService.class);

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(StandardElementRuntimeService.class);

    /**
     * Loads the ElementArtifactLoader SPI.
     */
    private static ElementArtifactLoader loadArtifactLoader() {
        try {
            final var loader = ServiceLoader.load(ElementArtifactLoader.class);
            final var optional = loader.findFirst();

            if (optional.isEmpty()) {
                throw new InternalException("ElementArtifactLoader SPI not available. Maven-based artifact loading is required.");
            }

            final var artifactLoader = optional.get();
            logger.info("ElementArtifactLoader SPI available: {}", artifactLoader.getClass().getName());
            return artifactLoader;

        } catch (InternalException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalException("Failed to load ElementArtifactLoader SPI. Maven-based artifact loading is required.", ex);
        }
    }

    private LargeObjectBucket largeObjectBucket;

    private ElementDeploymentDao elementDeploymentDao;

    private MutableElementRegistry rootElementRegistry;

    private int pollIntervalSeconds;

    private final Lock lock = new ReentrantLock();

    private final Map<String, ActiveDeployment> activeDeployments = new HashMap<>();

    private ScheduledExecutorService scheduler;

    private final ElementArtifactLoader elementArtifactLoader = loadArtifactLoader();

    @Override
    public void start() {
        try (var mon = Monitor.enter(lock)) {

            if (scheduler != null) {
                throw new IllegalStateException("Already started");
            }

            // Create scheduler with daemon thread
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                final var thread = new Thread(r, "element-runtime-service");
                thread.setDaemon(true);
                return thread;
            });

            // Schedule periodic reconciliation
            scheduler.scheduleAtFixedRate(
                    this::safeReconcile,
                    0,
                    getPollIntervalSeconds(),
                    TimeUnit.SECONDS
            );

            logger.info("ElementRuntimeService started with poll interval of {} seconds", getPollIntervalSeconds());

        }
    }

    @Override
    public void stop() {
        try (var mon = Monitor.enter(lock)) {

            if (scheduler == null) {
                throw new IllegalStateException("Already stopped");
            }

            logger.info("Stopping ElementRuntimeService...");

            // Shutdown scheduler
            scheduler.shutdown();

            try {
                if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException ex) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }

            scheduler = null;

            // Unload all active deployments
            final var deploymentIds = new ArrayList<>(activeDeployments.keySet());

            for (final String deploymentId : deploymentIds) {
                try {
                    doUnloadDeployment(deploymentId);
                } catch (Exception ex) {
                    logger.error("Error unloading deployment {} during shutdown", deploymentId, ex);
                }
            }

            activeDeployments.clear();
            logger.info("ElementRuntimeService stopped");

        }

    }

    @Override
    public List<RuntimeRecord> getActiveRuntimes() {
        try (var mon = Monitor.enter(lock)) {
            return activeDeployments.values()
                    .stream()
                    .map(active -> new RuntimeRecord(
                            active.deployment(),
                            active.status(),
                            active.registry(),
                            active.elements(),
                            active.logs(),
                            active.errors()
                    ))
                    .toList();
        }
    }

    /**
     * Safe wrapper around reconcile that catches all exceptions.
     */
    private void safeReconcile() {
        try {
            reconcile();
        } catch (Exception ex) {
            logger.error("Error during reconciliation", ex);
        }
    }

    /**
     * Reconciles database state with in-memory state.
     */
    private void reconcile() {

        try (var mon = Monitor.enter(lock)) {

            logger.debug("Starting reconciliation...");

            // Fetch all ENABLED deployments from database
            final List<ElementDeployment> enabledDeployments;

            try {
                enabledDeployments = getElementDeploymentDao().getElementDeploymentsByState(ElementDeploymentState.ENABLED);
            } catch (Exception ex) {
                logger.error("Failed to fetch enabled deployments from database", ex);
                return;
            }

            // Build set of deployment IDs from DB
            final var dbDeploymentIds = new HashSet<String>();

            for (final var deployment : enabledDeployments) {
                dbDeploymentIds.add(deployment.id());
            }

            // Process each deployment from the database
            for (final var deployment : enabledDeployments) {

                final var deploymentId = deployment.id();
                final var active = activeDeployments.get(deploymentId);

                if (active == null) {
                    // New deployment - load it
                    try {
                        doLoadDeployment(deployment);
                        logger.info("Loaded deployment: {}", deploymentId);
                    } catch (Exception ex) {
                        logger.error("Failed to load deployment: {}", deploymentId, ex);
                    }
                } else if (active.deployment().version() != deployment.version()) {
                    // Version changed - reload
                    try {
                        doUnloadDeployment(deploymentId);
                        doLoadDeployment(deployment);
                        logger.info("Reloaded deployment: {} (version {} -> {})",
                                deploymentId, active.deployment().version(), deployment.version());
                    } catch (Exception ex) {
                        logger.error("Failed to reload deployment: {}", deploymentId, ex);
                    }
                } else {
                    logger.debug("Deployment {} does not needed to be reloaded", deploymentId);
                }

            }

            // Unload deployments no longer in database (deleted or state changed)
            final var activeIds = new ArrayList<>(activeDeployments.keySet());

            for (final String activeId : activeIds) {
                if (!dbDeploymentIds.contains(activeId)) {
                    try {
                        doUnloadDeployment(activeId);
                        logger.info("Unloaded deployment: {}", activeId);
                    } catch (Exception ex) {
                        logger.error("Failed to unload deployment: {}", activeId, ex);
                    }
                }
            }

            logger.debug("Reconciliation complete. Active deployments: {}", activeDeployments.size());
        }

    }

    /**
     * Loads a deployment.
     */
    private void doLoadDeployment(final ElementDeployment deployment) {

        final var deploymentId = deployment.id();
        final var tempFiles = new ArrayList<Path>();
        final var logs = new ArrayList<String>();
        final var errors = new ArrayList<Throwable>();
        final var warnings = new ArrayList<String>();

        MutableElementRegistry registry = null;
        List<Element> elements = null;
        RuntimeStatus status = RuntimeStatus.CLEAN;

        try {

            logs.add("Starting deployment load for " + deploymentId);

            // Create subordinate registry for this deployment
            registry = getRootElementRegistry().newSubordinateRegistry();
            logs.add("Created subordinate registry");

            // Determine code source and load
            elements = loadElements(deployment, registry, tempFiles, logs, warnings, errors);
            logs.add("Loaded " + elements.size() + " element(s)");

            // Determine final status
            if (!errors.isEmpty()) {
                status = RuntimeStatus.UNSTABLE;
                logs.add("Deployment completed with " + errors.size() + " error(s)");
            } else if (!warnings.isEmpty()) {
                status = RuntimeStatus.WARNINGS;
                logs.add("Deployment completed with " + warnings.size() + " warning(s)");
            } else {
                logs.add("Deployment completed successfully");
            }

            // Track active deployment
            final var active = new ActiveDeployment(
                    deployment,
                    status,
                    registry,
                    elements,
                    tempFiles,
                    logs,
                    errors
            );

            activeDeployments.put(deploymentId, active);

        } catch (Exception ex) {
            logs.add("Deployment failed: " + ex.getMessage());
            errors.add(ex);
            status = RuntimeStatus.FAILED;

            // Store failed deployment
            final var failedDeployment = new ActiveDeployment(
                    deployment,
                    status,
                    registry,
                    elements != null ? elements : List.of(),
                    tempFiles,
                    logs,
                    errors
            );
            activeDeployments.put(deploymentId, failedDeployment);

            // Clean up on failure
            if (registry != null) {
                try {
                    registry.close();
                } catch (Exception closeEx) {
                    logger.warn("Error closing registry after failed load for deployment {}", deploymentId, closeEx);
                }
            }
            for (final Path tempFile : tempFiles) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ioEx) {
                    logger.warn("Failed to delete temp file {} after failed load", tempFile, ioEx);
                }
            }
        }
    }

    /**
     * Loads elements based on the deployment's code source strategy.
     */
    private List<Element> loadElements(final ElementDeployment deployment,
                                        final MutableElementRegistry registry,
                                        final List<Path> tempFiles,
                                        final List<String> logs,
                                        final List<String> warnings,
                                        final List<Throwable> errors) throws IOException {
        final var deploymentId = deployment.id();

        try {
            // Build repository set
            logs.add("Building artifact repository set");
            final var repositories = buildRepositorySet(deployment);
            if (repositories.isEmpty()) {
                warnings.add("No artifact repositories configured");
            } else {
                logs.add("Configured " + repositories.size() + " artifact repository(ies)");
            }

            // Build ClassLoader chain: System -> API -> SPI
            logs.add("Building ClassLoader chain");
            final var systemClassLoader = getClass().getClassLoader();

            final var apiClassLoader = resolveClassLoader(
                    systemClassLoader,
                    repositories,
                    deployment.apiArtifacts(),
                    logs,
                    warnings
            );

            final var spiClassLoader = resolveClassLoader(
                    apiClassLoader,
                    repositories,
                    deployment.spiArtifacts(),
                    logs,
                    warnings
            );

            // Determine code source strategy
            if (hasElmFromLargeObject(deployment)) {
                // Strategy 1: ELM from LargeObject
                logs.add("Loading from ELM file in LargeObject");
                return loadFromLargeObject(deployment, registry, spiClassLoader, tempFiles, logs, errors);
            } else if (deployment.elmArtifact() != null && !deployment.elmArtifact().isBlank()) {
                // Strategy 2: ELM artifact from Maven
                logs.add("Loading from ELM artifact: " + deployment.elmArtifact());
                return loadFromElmArtifact(deployment, registry, spiClassLoader, repositories, logs, errors);
            } else if (deployment.elementArtifacts() != null && !deployment.elementArtifacts().isEmpty()) {
                // Strategy 3: Maven artifact collection
                logs.add("Loading from element artifacts: " + deployment.elementArtifacts());
                return loadFromElementArtifacts(deployment, registry, spiClassLoader, repositories, logs, errors);
            } else {
                final var message = "Deployment " + deploymentId + " has no valid code source";
                logs.add("ERROR: " + message);
                throw new IllegalStateException(message);
            }
        } catch (Exception ex) {
            errors.add(ex);
            throw ex;
        }
    }

    /**
     * Checks if the deployment has an ELM file uploaded to LargeObject.
     */
    private boolean hasElmFromLargeObject(final ElementDeployment deployment) {
        return deployment.elm() != null &&
               LargeObjectState.UPLOADED.equals(deployment.elm().getState());
    }

    /**
     * Loads elements from an ELM file stored in LargeObject.
     */
    private List<Element> loadFromLargeObject(final ElementDeployment deployment,
                                               final MutableElementRegistry registry,
                                               final ClassLoader spiClassLoader,
                                               final List<Path> tempFiles,
                                               final List<String> logs,
                                               final List<Throwable> errors) throws IOException {
        try {
            final var elmRef = deployment.elm();
            final var elmId = elmRef.getId();

            logs.add("Downloading ELM file from LargeObject: " + elmId);

            // Download ELM to temp file
            final var tempPath = temporaryFiles.createTempFile(".elm");
            tempFiles.add(tempPath);

            try (final InputStream in = getLargeObjectBucket().readObject(elmId);
                 final OutputStream out = Files.newOutputStream(tempPath)) {
                in.transferTo(out);
            }

            logs.add("Downloaded ELM to temporary file: " + tempPath);

            // Load via ElementPathLoader
            final var pathLoader = ElementPathLoader.newDefaultInstance();
            logs.add("Loading Elements from ELM file");
            final var elements = pathLoader.load(registry, tempPath, spiClassLoader).toList();

            logs.add("Successfully loaded " + elements.size() + " element(s) from ELM file");
            return elements;

        } catch (Exception ex) {
            logs.add("Failed to load from LargeObject: " + ex.getMessage());
            errors.add(ex);
            throw ex;
        }
    }

    /**
     * Loads elements from an ELM artifact resolved via Maven.
     */
    private List<Element> loadFromElmArtifact(final ElementDeployment deployment,
                                               final MutableElementRegistry registry,
                                               final ClassLoader spiClassLoader,
                                               final Set<ArtifactRepository> repositories,
                                               final List<String> logs,
                                               final List<Throwable> errors) {
        try {

            logs.add("Resolving ELM artifact from repositories");
            // Resolve artifact
            final var artifact = elementArtifactLoader.getArtifact(repositories, deployment.elmArtifact());
            final var artifactPath = artifact.path();
            logs.add("Resolved ELM artifact to path: " + artifactPath);

            // Load via ElementPathLoader
            final var pathLoader = ElementPathLoader.newDefaultInstance();
            logs.add("Loading Elements from ELM artifact");
            final var elements = pathLoader.load(registry, artifactPath, spiClassLoader).toList();

            logs.add("Successfully loaded " + elements.size() + " element(s) from ELM artifact");
            return elements;

        } catch (Exception ex) {
            logs.add("Failed to load from ELM artifact: " + ex.getMessage());
            errors.add(ex);
            throw ex;
        }
    }

    /**
     * Loads elements from a collection of Maven artifacts.
     */
    private List<Element> loadFromElementArtifacts(final ElementDeployment deployment,
                                                    final MutableElementRegistry registry,
                                                    final ClassLoader spiClassLoader,
                                                    final Set<ArtifactRepository> repositories,
                                                    final List<String> logs,
                                                    final List<Throwable> errors) {
        try {

            logs.add("Resolving element artifacts from repositories");
            // Resolve classloader with element artifacts
            final var coords = new HashSet<>(deployment.elementArtifacts());
            final var elementClassLoader = elementArtifactLoader.getClassLoader(spiClassLoader, repositories, coords);
            logs.add("Created ClassLoader with element artifacts");

            // Use ElementLoaderFactory for isolated loading
            final var loaderFactory = ElementLoaderFactory.getDefault();
            final var attributes = SimpleAttributes.newDefaultInstance();

            logs.add("Creating isolated Element loader");
            // Get loader and load element
            final var loader = loaderFactory.getIsolatedLoader(
                    attributes,
                    elementClassLoader,
                    cl -> cl
            );

            logs.add("Registering Element with registry");
            final var element = registry.register(loader);
            logs.add("Successfully loaded 1 element from artifacts");

            return List.of(element);

        } catch (Exception ex) {
            logs.add("Failed to load from element artifacts: " + ex.getMessage());
            errors.add(ex);
            throw ex;
        }
    }

    /**
     * Builds the set of repositories for artifact resolution.
     */
    private Set<ArtifactRepository> buildRepositorySet(final ElementDeployment deployment) {
        final var result = new HashSet<ArtifactRepository>();

        // Add default repositories if requested
        if (deployment.useDefaultRepositories()) {
            result.addAll(ArtifactRepository.DEFAULTS);
        }

        // Add explicit repositories
        if (deployment.repositories() != null) {
            deployment.repositories()
                    .stream()
                    .map(ear -> new ArtifactRepository(ear.id(), ear.url()))
                    .forEach(result::add);
        }

        return result;
    }

    /**
     * Resolves a ClassLoader for the given artifacts, or returns the parent if no artifacts.
     */
    private ClassLoader resolveClassLoader(final ClassLoader parent,
                                           final Set<ArtifactRepository> repositories,
                                           final List<String> coordinates,
                                           final List<String> logs,
                                           final List<String> warnings) {
        if (coordinates == null || coordinates.isEmpty()) {
            return parent;
        }

        try {
            logs.add("Resolving ClassLoader for artifacts: " + coordinates);
            final var coords = new HashSet<>(coordinates);
            final Optional<ClassLoader> optional = elementArtifactLoader.findClassLoader(parent, repositories, coords);

            if (optional.isPresent()) {
                logs.add("Successfully resolved ClassLoader for " + coordinates.size() + " artifact(s)");
                return optional.get();
            } else {
                warnings.add("Could not resolve artifacts: " + coordinates);
                return parent;
            }
        } catch (Exception ex) {
            final var warning = "Error resolving artifacts " + coordinates + ": " + ex.getMessage();
            logger.warn(warning, ex);
            warnings.add(warning);
            return parent;
        }
    }

    /**
     * Unloads a deployment.
     */
    private void doUnloadDeployment(final String deploymentId) {
        final var active = activeDeployments.remove(deploymentId);
        if (active != null) {
            active.close();
        }
    }

    public MutableElementRegistry getRootElementRegistry() {
        return rootElementRegistry;
    }

    @Inject
    public void setRootElementRegistry(@Named(ROOT) final MutableElementRegistry rootElementRegistry) {
        this.rootElementRegistry = rootElementRegistry;
    }

    public ElementDeploymentDao getElementDeploymentDao() {
        return elementDeploymentDao;
    }

    @Inject
    public void setElementDeploymentDao(final ElementDeploymentDao elementDeploymentDao) {
        this.elementDeploymentDao = elementDeploymentDao;
    }

    public LargeObjectBucket getLargeObjectBucket() {
        return largeObjectBucket;
    }

    @Inject
    public void setLargeObjectBucket(final LargeObjectBucket largeObjectBucket) {
        this.largeObjectBucket = largeObjectBucket;
    }

    public int getPollIntervalSeconds() {
        return pollIntervalSeconds;
    }

    @Inject
    public void setPollIntervalSeconds(@Named(POLL_INTERVAL_SECONDS) final int pollIntervalSeconds) {
        this.pollIntervalSeconds = pollIntervalSeconds;
    }

    /**
     * Tracks an active deployment's runtime state.
     */
    private record ActiveDeployment(
            ElementDeployment deployment,
            RuntimeStatus status,
            MutableElementRegistry registry,
            List<Element> elements,
            List<Path> tempFiles,
            List<String> logs,
            List<Throwable> errors
    ) implements AutoCloseable {

        ActiveDeployment {
            logs = logs != null ? List.copyOf(logs) : List.of();
            errors = errors != null ? List.copyOf(errors) : List.of();
        }

        @Override
        public void close() {

            // Close the registry (which closes all elements)
            try {
                registry.close();
            } catch (Exception ex) {
                logger.error("Error closing registry for deployment {}", deployment.id(), ex);
            }

            // Clean up temp files
            for (final Path tempFile : tempFiles) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ex) {
                    logger.warn("Failed to delete temp file {} for deployment {}", tempFile, deployment.id(), ex);
                }
            }

        }
    }

}

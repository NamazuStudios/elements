package dev.getelements.elements.common.app;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
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

    private static final Logger logger = LoggerFactory.getLogger(StandardElementRuntimeService.class);

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(StandardElementRuntimeService.class);

    private LargeObjectBucket largeObjectBucket;

    private ElementDeploymentDao elementDeploymentDao;

    private MutableElementRegistry rootElementRegistry;

    private int pollIntervalSeconds;

    private final Lock lock = new ReentrantLock();

    private final Map<String, ActiveDeployment> activeDeployments = new HashMap<>();

    private ScheduledExecutorService scheduler;

    private ElementArtifactLoader elementArtifactLoader;

    private boolean artifactLoaderAvailable;

    @Override
    public void start() {
        try (var mon = Monitor.enter(lock)) {

            if (scheduler != null) {
                throw new IllegalStateException("Already started");
            }

            // Initialize artifact loader SPI
            initializeArtifactLoader();

            // Create scheduler with daemon thread
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                final var thread = new Thread(r, "element-runtime-service");
                thread.setDaemon(true);
                return thread;
            });

            // Run initial reconcile immediately
            scheduler.execute(this::safeReconcile);

            // Schedule periodic reconciliation
            scheduler.scheduleAtFixedRate(
                    this::safeReconcile,
                    pollIntervalSeconds,
                    pollIntervalSeconds,
                    TimeUnit.SECONDS
            );

            logger.info("ElementRuntimeService started with poll interval of {} seconds", pollIntervalSeconds);

        }
    }

    @Override
    public void stop() {
        try (var mon = Monitor.enter(lock)) {
            if (scheduler == null) {
                logger.warn("ElementRuntimeService not started");
                return;
            }

            logger.info("Stopping ElementRuntimeService...");

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

            // Unload all active deployments
            final var deploymentIds = new ArrayList<>(activeDeployments.keySet());
            for (final String deploymentId : deploymentIds) {
                try {
                    doUnloadDeployment(deploymentId);
                } catch (Exception ex) {
                    logger.error("Error unloading deployment {} during shutdown", deploymentId, ex);
                }
            }

            logger.info("ElementRuntimeService stopped");
        }
    }

    /**
     * Initializes the ElementArtifactLoader SPI.
     */
    private void initializeArtifactLoader() {
        try {
            final var loader = ServiceLoader.load(ElementArtifactLoader.class);
            final var optional = loader.findFirst();
            if (optional.isPresent()) {
                elementArtifactLoader = optional.get();
                artifactLoaderAvailable = true;
                logger.info("ElementArtifactLoader SPI available: {}", elementArtifactLoader.getClass().getName());
            } else {
                artifactLoaderAvailable = false;
                logger.warn("ElementArtifactLoader SPI not available. Maven-based artifact loading disabled.");
            }
        } catch (Exception ex) {
            artifactLoaderAvailable = false;
            logger.warn("Failed to load ElementArtifactLoader SPI. Maven-based artifact loading disabled.", ex);
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
                } else if (active.version() != deployment.version()) {
                    // Version changed - reload
                    try {
                        doUnloadDeployment(deploymentId);
                        doLoadDeployment(deployment);
                        logger.info("Reloaded deployment: {} (version {} -> {})",
                                deploymentId, active.version(), deployment.version());
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
    private void doLoadDeployment(final ElementDeployment deployment) throws IOException {

        final var deploymentId = deployment.id();
        
        final var tempFiles = new ArrayList<Path>();

        // Create subordinate registry for this deployment
        final var registry = getRootElementRegistry().newSubordinateRegistry();

        try {
            // Determine code source and load
            final var elements = loadElements(deployment, registry, tempFiles);

            // Track active deployment
            final var active = new ActiveDeployment(
                    deploymentId,
                    deployment.version(),
                    registry,
                    elements,
                    tempFiles
            );

            activeDeployments.put(deploymentId, active);

        } catch (Exception ex) {
            // Clean up on failure
            try {
                registry.close();
            } catch (Exception closeEx) {
                logger.warn("Error closing registry after failed load for deployment {}", deploymentId, closeEx);
            }
            for (final Path tempFile : tempFiles) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ioEx) {
                    logger.warn("Failed to delete temp file {} after failed load", tempFile, ioEx);
                }
            }
            throw ex;
        }
    }

    /**
     * Loads elements based on the deployment's code source strategy.
     */
    private List<Element> loadElements(final ElementDeployment deployment,
                                        final MutableElementRegistry registry,
                                        final List<Path> tempFiles) throws IOException {
        final var deploymentId = deployment.id();

        // Build repository set
        final var repositories = buildRepositorySet(deployment);

        // Build ClassLoader chain: System -> API -> SPI
        final var systemClassLoader = getClass().getClassLoader();
        final var apiClassLoader = resolveClassLoader(systemClassLoader, repositories, deployment.apiArtifacts());
        final var spiClassLoader = resolveClassLoader(apiClassLoader, repositories, deployment.spiArtifacts());

        // Determine code source strategy
        if (hasElmFromLargeObject(deployment)) {
            // Strategy 1: ELM from LargeObject
            return loadFromLargeObject(deployment, registry, spiClassLoader, tempFiles);
        } else if (deployment.elmArtifact() != null && !deployment.elmArtifact().isBlank()) {
            // Strategy 2: ELM artifact from Maven
            return loadFromElmArtifact(deployment, registry, spiClassLoader, repositories);
        } else if (deployment.elementArtifacts() != null && !deployment.elementArtifacts().isEmpty()) {
            // Strategy 3: Maven artifact collection
            return loadFromElementArtifacts(deployment, registry, spiClassLoader, repositories);
        } else {
            throw new IllegalStateException("Deployment " + deploymentId + " has no valid code source");
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
                                               final List<Path> tempFiles) throws IOException {
        final var elmRef = deployment.elm();
        final var elmId = elmRef.getId();

        // Download ELM to temp file
        final var tempPath = temporaryFiles.createTempFile(".elm");
        tempFiles.add(tempPath);

        try (final InputStream in = getLargeObjectBucket().readObject(elmId);
             final OutputStream out = Files.newOutputStream(tempPath)) {
            in.transferTo(out);
        }

        // Load via ElementPathLoader
        final var pathLoader = ElementPathLoader.newDefaultInstance();
        return pathLoader.load(registry, tempPath, spiClassLoader).toList();
    }

    /**
     * Loads elements from an ELM artifact resolved via Maven.
     */
    private List<Element> loadFromElmArtifact(final ElementDeployment deployment,
                                               final MutableElementRegistry registry,
                                               final ClassLoader spiClassLoader,
                                               final Set<ArtifactRepository> repositories) {
        if (!artifactLoaderAvailable) {
            throw new IllegalStateException(
                    "Deployment " + deployment.id() + " requires ELM artifact but ElementArtifactLoader SPI not available");
        }

        // Resolve artifact
        final var artifact = elementArtifactLoader.getArtifact(repositories, deployment.elmArtifact());
        final var artifactPath = artifact.path();

        // Load via ElementPathLoader
        final var pathLoader = ElementPathLoader.newDefaultInstance();
        return pathLoader.load(registry, artifactPath, spiClassLoader).toList();
    }

    /**
     * Loads elements from a collection of Maven artifacts.
     */
    private List<Element> loadFromElementArtifacts(final ElementDeployment deployment,
                                                    final MutableElementRegistry registry,
                                                    final ClassLoader spiClassLoader,
                                                    final Set<ArtifactRepository> repositories) {
        if (!artifactLoaderAvailable) {
            throw new IllegalStateException(
                    "Deployment " + deployment.id() + " requires element artifacts but ElementArtifactLoader SPI not available");
        }

        // Resolve classloader with element artifacts
        final var coords = new HashSet<>(deployment.elementArtifacts());
        final var elementClassLoader = elementArtifactLoader.getClassLoader(spiClassLoader, repositories, coords);

        // Use ElementLoaderFactory for isolated loading
        final var loaderFactory = ElementLoaderFactory.getDefault();
        final var attributes = SimpleAttributes.newDefaultInstance();

        // Get loader and load element
        final var loader = loaderFactory.getIsolatedLoader(
                attributes,
                elementClassLoader,
                cl -> cl
        );

        final var element = registry.register(loader);
        return List.of(element);
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
            result.addAll(deployment.repositories());
        }

        return result;
    }

    /**
     * Resolves a ClassLoader for the given artifacts, or returns the parent if no artifacts.
     */
    private ClassLoader resolveClassLoader(final ClassLoader parent,
                                           final Set<ArtifactRepository> repositories,
                                           final List<String> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return parent;
        }

        if (!artifactLoaderAvailable) {
            logger.warn("Cannot resolve artifacts {} - ElementArtifactLoader SPI not available", coordinates);
            return parent;
        }

        final var coords = new HashSet<>(coordinates);
        final Optional<ClassLoader> optional = elementArtifactLoader.findClassLoader(parent, repositories, coords);
        return optional.orElse(parent);
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
            String deploymentId,
            long version,
            MutableElementRegistry registry,
            List<Element> elements,
            List<Path> tempFiles
    ) implements AutoCloseable {

        @Override
        public void close() {

            // Close the registry (which closes all elements)
            try {
                registry.close();
            } catch (Exception ex) {
                logger.error("Error closing registry for deployment {}", deploymentId, ex);
            }

            // Clean up temp files
            for (final Path tempFile : tempFiles) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ex) {
                    logger.warn("Failed to delete temp file {} for deployment {}", tempFile, deploymentId, ex);
                }
            }

        }
    }

}

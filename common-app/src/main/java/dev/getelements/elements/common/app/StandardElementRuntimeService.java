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
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

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

            // Create deployment directory structure
            logs.add("Creating deployment directory structure");
            final var deploymentDir = temporaryFiles.createTempDirectory(deploymentId);
            tempFiles.add(deploymentDir);
            logs.add("Created deployment directory: " + deploymentDir);

            final var allElements = new ArrayList<Element>();

            // Strategy 1: Load from uploaded ELM file if present
            if (hasElmFromLargeObject(deployment)) {
                logs.add("Loading elements from uploaded ELM file");
                final var elmElements = loadFromLargeObject(deployment, registry, tempFiles, logs, errors, repositories);
                allElements.addAll(elmElements);
            }

            // Strategy 2: Process each ElementDefinition
            if (deployment.elements() != null && !deployment.elements().isEmpty()) {
                logs.add("Processing " + deployment.elements().size() + " element definition(s)");

                for (int i = 0; i < deployment.elements().size(); i++) {
                    final var definition = deployment.elements().get(i);
                    logs.add("Processing element definition " + (i + 1) + "/" + deployment.elements().size());

                    final var elements = loadFromElementDefinition(
                            definition,
                            deployment,
                            deploymentDir,
                            registry,
                            repositories,
                            tempFiles,
                            logs,
                            warnings,
                            errors
                    );
                    allElements.addAll(elements);
                }
            }

            // Strategy 3: Process each ElementPackageDefinition
            if (deployment.packages() != null && !deployment.packages().isEmpty()) {
                logs.add("Processing " + deployment.packages().size() + " element package(s)");

                for (int i = 0; i < deployment.packages().size(); i++) {
                    final var packageDef = deployment.packages().get(i);
                    logs.add("Processing element package " + (i + 1) + "/" + deployment.packages().size());

                    final var elements = loadFromPackageDefinition(
                            packageDef,
                            deployment,
                            deploymentDir,
                            registry,
                            repositories,
                            tempFiles,
                            logs,
                            warnings,
                            errors
                    );
                    allElements.addAll(elements);
                }
            }

            if (allElements.isEmpty()) {
                final var message = "Deployment " + deploymentId + " produced no elements";
                logs.add("ERROR: " + message);
                throw new IllegalStateException(message);
            }

            logs.add("Successfully loaded " + allElements.size() + " total element(s)");
            return allElements;

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
                                               final List<Path> tempFiles,
                                               final List<String> logs,
                                               final List<Throwable> errors,
                                               final Set<ArtifactRepository> repositories) throws IOException {
        try {
            final var elmRef = deployment.elm();
            final var elmId = elmRef.getId();

            logs.add("Downloading ELM file from LargeObject: " + elmId);

            // Download ELM to temp file
            final var tempPath = temporaryFiles.createTempFile(deployment.id(), ".elm");
            tempFiles.add(tempPath);

            try (final InputStream in = getLargeObjectBucket().readObject(elmId);
                 final OutputStream out = Files.newOutputStream(tempPath)) {
                in.transferTo(out);
            }

            logs.add("Downloaded ELM to temporary file: " + tempPath);

            // Load via ElementPathLoader
            final var pathLoader = ElementPathLoader.newDefaultInstance();

            // Build API classloader from the ELM
            logs.add("Building API classloader from ELM");
            final var systemClassLoader = getClass().getClassLoader();
            final var apiClassLoader = pathLoader.buildApiClassLoader(null, tempPath);

            logs.add("Loading Elements from ELM file");
            final var elements = pathLoader.load(registry, tempPath, apiClassLoader).toList();

            logs.add("Successfully loaded " + elements.size() + " element(s) from ELM file");
            return elements;

        } catch (Exception ex) {
            logs.add("Failed to load from LargeObject: " + ex.getMessage());
            errors.add(ex);
            throw ex;
        }
    }

    /**
     * Loads elements from a single ElementDefinition.
     */
    private List<Element> loadFromElementDefinition(
            final dev.getelements.elements.sdk.model.system.ElementDefinition definition,
            final ElementDeployment deployment,
            final Path deploymentDir,
            final MutableElementRegistry registry,
            final Set<ArtifactRepository> repositories,
            final List<Path> tempFiles,
            final List<String> logs,
            final List<String> warnings,
            final List<Throwable> errors) throws IOException {
        try {
            final var systemClassLoader = getClass().getClassLoader();

            // Check if this definition uses an ELM artifact with path-based deployment
            if (definition.elmArtifact() != null && !definition.elmArtifact().isBlank()) {

                if (definition.path() != null && !definition.path().isBlank()) {
                    // Strategy: Structured deployment with ELM + separate artifacts
                    logs.add("Loading from ELM artifact with structured deployment: " + definition.elmArtifact());
                    logs.add("Deployment path: " + definition.path());

                    // Create element path directory
                    final var elementPath = deploymentDir.resolve(definition.path());
                    Files.createDirectories(elementPath);
                    logs.add("Created element directory: " + elementPath);

                    // Create subdirectories for artifacts
                    final var apiDir = elementPath.resolve(ElementPathLoader.API_DIR);
                    final var spiDir = elementPath.resolve(ElementPathLoader.SPI_DIR);
                    final var libDir = elementPath.resolve(ElementPathLoader.LIB_DIR);
                    Files.createDirectories(apiDir);
                    Files.createDirectories(spiDir);
                    Files.createDirectories(libDir);

                    // Gather and place API artifacts
                    if (definition.apiArtifacts() != null && !definition.apiArtifacts().isEmpty()) {
                        logs.add("Gathering " + definition.apiArtifacts().size() + " API artifact(s)");
                        for (final var coordinate : definition.apiArtifacts()) {
                            copyArtifactWithDependencies(coordinate, repositories, apiDir, logs);
                        }
                    }

                    // Gather and place SPI artifacts
                    if (definition.spiArtifacts() != null && !definition.spiArtifacts().isEmpty()) {
                        logs.add("Gathering " + definition.spiArtifacts().size() + " SPI artifact(s)");
                        for (final var coordinate : definition.spiArtifacts()) {
                            copyArtifactWithDependencies(coordinate, repositories, spiDir, logs);
                        }
                    }

                    // Gather and place implementation artifacts (from elementArtifacts or elmArtifact)
                    if (definition.elementArtifacts() != null && !definition.elementArtifacts().isEmpty()) {
                        logs.add("Gathering " + definition.elementArtifacts().size() + " element artifact(s)");
                        for (final var coordinate : definition.elementArtifacts()) {
                            copyArtifactWithDependencies(coordinate, repositories, libDir, logs);
                        }
                    }

                    // Also copy the ELM artifact itself to lib
                    logs.add("Resolving ELM artifact: " + definition.elmArtifact());
                    final var elmArtifact = elementArtifactLoader.getArtifact(repositories, definition.elmArtifact());
                    final var elmDestination = libDir.resolve(elmArtifact.path().getFileName());
                    Files.copy(elmArtifact.path(), elmDestination, StandardCopyOption.REPLACE_EXISTING);
                    logs.add("Copied ELM artifact to: " + elmDestination);

                    // Load using LoadConfiguration with AttributesLoader
                    final var pathLoader = ElementPathLoader.newDefaultInstance();
                    final var baseAttributes = new SimpleAttributes.Builder()
                            .setAttributes(deployment.attributes())
                            .build();

                    final var config = ElementPathLoader.LoadConfiguration.builder()
                            .registry(registry)
                            .path(elementPath)
                            .attributesProvider((attrs, path) -> {
                                // Merge deployment attributes with element-specific attributes
                                final var builder = new SimpleAttributes.Builder().from(baseAttributes);
                                if (definition.attributes() != null) {
                                    builder.setAttributes(definition.attributes());
                                }
                                return builder.build();
                            })
                            .build();

                    final var elements = pathLoader.load(config).toList();
                    logs.add("Loaded " + elements.size() + " element(s) from structured deployment");

                    for (final var element : elements) {
                        final var elementName = element.getElementRecord().definition().name();
                        logs.add("  - Element: " + elementName);
                    }

                    return elements;

                } else {
                    // Legacy strategy: Load ELM directly without structured deployment
                    logs.add("Loading from ELM artifact (legacy): " + definition.elmArtifact());

                    // Resolve the ELM artifact
                    final var artifact = elementArtifactLoader.getArtifact(repositories, definition.elmArtifact());
                    final var artifactPath = artifact.path();
                    logs.add("Resolved ELM artifact to path: " + artifactPath);

                    // Load elements from the ELM using LoadConfiguration
                    final var pathLoader = ElementPathLoader.newDefaultInstance();

                    final var config = ElementPathLoader.LoadConfiguration.builder()
                            .registry(registry)
                            .path(artifactPath)
                            .attributesProvider((attrs, path) -> {
                                final var builder = new SimpleAttributes.Builder()
                                        .setAttributes(deployment.attributes());
                                if (definition.attributes() != null) {
                                    builder.setAttributes(definition.attributes());
                                }
                                return builder.build();
                            })
                            .build();

                    final var elements = pathLoader.load(config).toList();
                    logs.add("Loaded " + elements.size() + " element(s) from ELM artifact");

                    for (final var element : elements) {
                        final var elementName = element.getElementRecord().definition().name();
                        logs.add("  - Element: " + elementName);
                    }

                    return elements;
                }

            } else {
                // Build classloader chain from Maven artifacts: System -> (API+SPI) -> Element
                logs.add("Building classloader chain from Maven artifacts");

                // Combine API and SPI artifacts into a single classloader
                final var apiSpiCoordinates = new ArrayList<String>();
                if (definition.apiArtifacts() != null) {
                    apiSpiCoordinates.addAll(definition.apiArtifacts());
                }
                if (definition.spiArtifacts() != null) {
                    apiSpiCoordinates.addAll(definition.spiArtifacts());
                }

                final var apiSpiClassLoader = resolveClassLoader(
                        systemClassLoader,
                        repositories,
                        apiSpiCoordinates,
                        logs,
                        warnings
                );

                final var elementClassLoader = resolveClassLoader(
                        apiSpiClassLoader,
                        repositories,
                        definition.elementArtifacts(),
                        logs,
                        warnings
                );

                // Use ElementLoaderFactory for isolated loading
                logs.add("Creating isolated Element loader");
                final var loaderFactory = ElementLoaderFactory.getDefault();
                final var attributes = SimpleAttributes.newDefaultInstance();

                final var loader = loaderFactory.getIsolatedLoader(
                        attributes,
                        elementClassLoader,
                        cl -> cl
                );

                logs.add("Registering Element with registry");
                final var element = registry.register(loader);
                final var elementName = element.getElementRecord().definition().name();
                logs.add("Successfully loaded 1 element from Maven artifacts");
                logs.add("  - Element: " + elementName);

                return List.of(element);
            }

        } catch (Exception ex) {
            logs.add("Failed to load from ElementDefinition: " + ex.getMessage());
            errors.add(ex);
            throw ex;
        }
    }

    /**
     * Loads elements from an ElementPackageDefinition.
     */
    private List<Element> loadFromPackageDefinition(
            final dev.getelements.elements.sdk.model.system.ElementPackageDefinition packageDef,
            final ElementDeployment deployment,
            final Path deploymentDir,
            final MutableElementRegistry registry,
            final Set<ArtifactRepository> repositories,
            final List<Path> tempFiles,
            final List<String> logs,
            final List<String> warnings,
            final List<Throwable> errors) throws IOException {
        try {
            logs.add("Loading from package ELM artifact: " + packageDef.elmArtifact());

            // Resolve the ELM artifact
            final var elmArtifact = elementArtifactLoader.getArtifact(repositories, packageDef.elmArtifact());
            final var elmPath = elmArtifact.path();
            logs.add("Resolved package ELM to: " + elmPath);

            // Load elements using LoadConfiguration with per-path attributes
            final var pathLoader = ElementPathLoader.newDefaultInstance();

            final var config = ElementPathLoader.LoadConfiguration.builder()
                    .registry(registry)
                    .path(elmPath)
                    .attributesProvider((attrs, elementPath) -> {
                        // Build base attributes from deployment
                        final var builder = new SimpleAttributes.Builder()
                                .setAttributes(deployment.attributes());

                        // Apply per-path attributes if configured
                        if (packageDef.pathAttributes() != null) {
                            final var pathStr = elementPath.toString();
                            final var pathAttrs = packageDef.pathAttributes().get(pathStr);
                            if (pathAttrs != null) {
                                logs.add("Applying path-specific attributes for: " + pathStr);
                                builder.setAttributes(pathAttrs);
                            }
                        }

                        return builder.build();
                    })
                    .build();

            final var elements = pathLoader.load(config).toList();
            logs.add("Loaded " + elements.size() + " element(s) from package");

            for (final var element : elements) {
                final var elementName = element.getElementRecord().definition().name();
                logs.add("  - Element: " + elementName);
            }

            return elements;

        } catch (Exception ex) {
            logs.add("Failed to load from ElementPackageDefinition: " + ex.getMessage());
            errors.add(ex);
            throw ex;
        }
    }

    /**
     * Copies an artifact and its transitive dependencies to a target directory.
     */
    private void copyArtifactWithDependencies(
            final String coordinates,
            final Set<ArtifactRepository> repositories,
            final Path targetDir,
            final List<String> logs) throws IOException {

        logs.add("Resolving artifact with dependencies: " + coordinates);

        final var artifacts = elementArtifactLoader.findClasspathForArtifact(repositories, coordinates).toList();

        logs.add("Found " + artifacts.size() + " artifact(s) including dependencies");

        for (final var artifact : artifacts) {
            final var sourcePath = artifact.path();
            final var fileName = sourcePath.getFileName();
            final var destinationPath = targetDir.resolve(fileName);

            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            logs.add("Copied artifact: " + fileName);
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

package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.annotation.ElementServiceReference;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectState;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.model.system.ElementPackageDefinition;
import dev.getelements.elements.sdk.model.system.ElementPathDefinition;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.util.Objects.requireNonNull;

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

    private final AtomicLong transientCounter = new AtomicLong();

    private final Map<String, ActiveDeployment> activeDeployments = new HashMap<>();

    private final ElementArtifactLoader elementArtifactLoader = loadArtifactLoader();

    private final ElementPathLoader pathLoader = ElementPathLoader.newDefaultInstance();

    private ValidationHelper validationHelper;

    private ScheduledExecutorService scheduler;

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

        // Publish event OUTSIDE the lock to prevent double-locking issues
        publishRuntimeServiceStarted();
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

        // Publish event OUTSIDE the lock to prevent double-locking issues
        publishRuntimeServiceStopped();
    }

    @Override
    public List<RuntimeRecord> getActiveRuntimes() {
        try (var mon = Monitor.enter(lock)) {
            return activeDeployments.values()
                    .stream()
                    .map(ActiveDeployment::toRuntimeRecord)
                    .toList();
        }
    }

    @Override
    public RuntimeRecord loadTransientDeployment(final TransientDeploymentRequest request) {
        RuntimeRecord record;

        try (var mon = Monitor.enter(lock)) {

            if (scheduler == null) {
                throw new IllegalStateException("Service not started");
            }

            // Validate the request
            getValidationHelper().validateModel(request);

            // Generate unique ID for transient deployment
            final var deploymentId = "t%012X".formatted(transientCounter.incrementAndGet());

            // Check for ID collision (extremely unlikely but safe)
            if (activeDeployments.containsKey(deploymentId)) {
                throw new IllegalArgumentException("Deployment ID collision: " + deploymentId);
            }

            logger.info("Loading transient deployment with ID: {}", deploymentId);

            // Build ElementDeployment from request
            final var deployment = new ElementDeployment(
                    deploymentId,
                    request.application(),
                    null,
                    request.pathSpiClasspath(),
                    request.pathAttributes(),
                    request.elements(),
                    request.packages(),
                    request.useDefaultRepositories(),
                    request.repositories(),
                    ElementDeploymentState.ENABLED,
                    0L  // version not relevant for transient
            );

            // Load the deployment
            record = doLoadDeployment(deployment, true);

        }

        // Publish event OUTSIDE the lock
        publishRuntimeLoaded(record);

        return record;
    }

    @Override
    public boolean unloadTransientDeployment(final String deploymentId) {
        boolean unloaded;

        try (var mon = Monitor.enter(lock)) {

            if (scheduler == null) {
                throw new IllegalStateException("Service not started");
            }

            // Check if deployment exists
            final var active = activeDeployments.get(deploymentId);
            if (active == null) {
                logger.warn("Deployment not found: {}", deploymentId);
                return false;
            }

            // Check if it's a transient deployment
            if (!active.isTransient()) {
                logger.warn("Attempted to unload non-transient deployment: {}", deploymentId);
                return false;
            }

            logger.info("Unloading transient deployment: {}", deploymentId);

            // Unload the deployment
            doUnloadDeployment(deploymentId);
            unloaded = true;

        }

        // Publish event OUTSIDE the lock
        if (unloaded) {
            publishRuntimeUnloaded(deploymentId);
        }

        return unloaded;
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

        // Collect events to publish outside the lock
        final var loadedRecords = new ArrayList<RuntimeRecord>();
        final var unloadedDeploymentIds = new ArrayList<String>();

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
                        final var record = doLoadDeployment(deployment, false);  // Not transient
                        if (record != null) {
                            loadedRecords.add(record);
                        }
                        logger.info("Loaded deployment: {}", deploymentId);
                    } catch (Exception ex) {
                        logger.error("Failed to load deployment: {}", deploymentId, ex);
                    }
                } else if (active.deployment().version() != deployment.version()) {
                    // Version changed - reload
                    try {
                        doUnloadDeployment(deploymentId);
                        unloadedDeploymentIds.add(deploymentId);

                        final var record = doLoadDeployment(deployment, false);  // Not transient
                        if (record != null) {
                            loadedRecords.add(record);
                        }
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
            // Skip transient deployments - they are managed separately
            for (final var entry : activeDeployments.entrySet()) {
                final var activeId = entry.getKey();
                final var active = entry.getValue();

                if (!dbDeploymentIds.contains(activeId) && !active.isTransient()) {
                    try {
                        doUnloadDeployment(activeId);
                        unloadedDeploymentIds.add(activeId);
                        logger.info("Unloaded deployment: {}", activeId);
                    } catch (Exception ex) {
                        logger.error("Failed to unload deployment: {}", activeId, ex);
                    }
                }
            }

            logger.debug("Reconciliation complete. Active deployments: {}", activeDeployments.size());
        }

        // Publish events OUTSIDE the lock
        for (final var record : loadedRecords) {
            publishRuntimeLoaded(record);
        }

        for (final var deploymentId : unloadedDeploymentIds) {
            publishRuntimeUnloaded(deploymentId);
        }

    }

    /**
     * Loads a deployment.
     * NOTE: This method is called within a lock. Caller must publish events outside the lock.
     * @return the RuntimeRecord for the loaded deployment, or null if load failed catastrophically
     */
    private RuntimeRecord doLoadDeployment(final ElementDeployment deployment, final boolean isTransient) {

        final var deploymentId = deployment.id();
        final var tempFiles = new ArrayList<Path>();
        final var logs = new ArrayList<String>();
        final var errors = new ArrayList<Throwable>();
        final var fileSystems = new ArrayList<FileSystem>();

        MutableElementRegistry registry = null;
        List<Element> elements = null;
        RuntimeStatus status = RuntimeStatus.CLEAN;

        try {

            logs.add("Starting deployment load for " + deploymentId);

            // Create subordinate registry for this deployment
            registry = getRootElementRegistry().newSubordinateRegistry();
            logs.add("Created subordinate registry");

            // Determine code source and load
            final var context = new DeploymentContext(
                    deployment,
                    registry,
                    tempFiles,
                    fileSystems,
                    logs,
                    new ArrayList<>(),
                    errors,
                    new ArrayList<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    elementArtifactLoader,
                    new HashSet<>(),
                    temporaryFiles
            );

            elements = loadElements(context);
            logs.add("Loaded " + elements.size() + " element(s)");

            // Determine final status
            if (!context.errors().isEmpty()) {
                status = RuntimeStatus.UNSTABLE;
                logs.add("Deployment completed with " + errors.size() + " error(s)");
            } else if (!context.warnings().isEmpty()) {
                status = RuntimeStatus.WARNINGS;
                logs.add("Deployment completed with " + context.warnings().size() + " warning(s)");
            } else {
                logs.add("Deployment completed successfully");
            }

            // Track active deployment
            final var active = new ActiveDeployment(
                    deployment,
                    status,
                    isTransient,
                    registry,
                    elements,
                    context.tempFiles(),
                    context.fileSystems(),
                    context.logs(),
                    context.errors()
            );

            activeDeployments.put(deploymentId, active);

            // Return runtime record for event publishing (outside lock)
            return active.toRuntimeRecord();

        } catch (Exception ex) {

            logs.add("Deployment failed: " + ex.getMessage());
            errors.add(ex);
            status = RuntimeStatus.FAILED;

            // Store failed deployment
            final var failedDeployment = new ActiveDeployment(
                    deployment,
                    status,
                    isTransient,
                    registry,
                    elements != null ? elements : List.of(),
                    tempFiles,
                    fileSystems,
                    logs,
                    errors
            );

            activeDeployments.put(deploymentId, failedDeployment);

            // Return failed runtime record for event publishing
            final var failedRecord = failedDeployment.toRuntimeRecord();

            // Clean up on failure
            if (registry != null) {
                try {
                    registry.close();
                } catch (Exception closeEx) {
                    ex.addSuppressed(closeEx);
                    logger.warn("Error closing registry after failed load for deployment {}", deploymentId, closeEx);
                }
            }

            for (final Path tempFile : tempFiles) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ioEx) {
                    ex.addSuppressed(ioEx);
                    logger.warn("Failed to delete temp file {} after failed load", tempFile, ioEx);
                }
            }

            return failedRecord;
        }
    }

    /**
     * Loads elements based on the deployment's code source strategy.
     * Uses a stage-then-load approach: first stages all element sources into paths,
     * then loads all elements in a single operation using ElementPathLoader.
     */
    private List<Element> loadElements(final DeploymentContext context) {

        final var deploymentId = context.deployment().id();

        try {

            // Build repository set
            context.logs().add("Building artifact repository set");

            if (context.repositories().isEmpty()) {
                context.warnings().add("No artifact repositories configured");
            } else {
                context.repositories().forEach(r -> {
                    if (r.isDefault()) {
                        context.logs().add("Using default repository.");
                    } else {
                        context.logs().add("Using repository %s at %s".formatted(r.id(), r.url()));
                    }
                });
            }

            // Create deployment directory structure
            context.logs().add("Creating deployment directory structure");

            // Create shared PermittedTypesClassLoader for this deployment
            context.logs().add("Creating PermittedTypesClassLoader for deployment");
            final var permittedTypesClassLoader = new PermittedTypesClassLoader();

            // PHASE A: STAGING
            // Collect all element source paths into a list
            context.logs().add("=== Phase A: Staging all element sources ===");

            // Strategy 1: Stage uploaded ELM from LargeObject if present
            if (hasElmFromLargeObject(context.deployment())) {
                context.logs().add("Staging elements from uploaded ELM file");
                try {
                    stageFromLargeObject(context.deployment(), context);
                } catch (Exception ex) {
                    context.logs().add("Failed to stage from LargeObject: " + ex.getMessage());
                    context.errors().add(ex);
                }
            } else if (context.deployment().elm() != null) {
                context.logs().add("ELM LargeObject is in state %s".formatted(context.deployment().elm().getState()));
            } else {
                context.logs().add("No ELM configured");
            }

            // Strategy 2: Stage each ElementDefinition
            if (context.deployment().elements() != null && !context.deployment().elements().isEmpty()) {

                final var deploymentDir = temporaryFiles.createTempDirectory(deploymentId);
                context.elementPaths().add(deploymentDir);

                context.logs().add("Created deployment directory: " + deploymentDir);
                context.tempFiles().add(deploymentDir);

                context.logs().add("Staging " + context.deployment().elements().size() + " element definition(s)");

                for (int i = 0; i < context.deployment().elements().size(); i++) {

                    final var definition = context.deployment().elements().get(i);
                    context.logs().add("Staging element definition " + (i + 1) + "/" + context
                            .deployment()
                            .elements()
                            .size()
                    );

                    try {
                        stageFromElementDefinition(
                                definition,
                                deploymentDir,
                                context
                        );
                    } catch (Exception ex) {
                        context.logs().add("Failed to stage element definition: " + ex.getMessage());
                        context.errors().add(ex);
                    }

                }
            }

            // Strategy 3: Stage each ElementPackageDefinition
            if (context.deployment().packages() != null && !context.deployment().packages().isEmpty()) {

                context.logs().add("Staging " + context.deployment().packages().size() + " element package(s)");

                for (int i = 0; i < context.deployment().packages().size(); i++) {
                    final var packageDef = context.deployment().packages().get(i);
                    context.logs().add("Staging element package " + (i + 1) + "/" + context
                            .deployment()
                            .packages()
                            .size()
                    );

                    try {
                        stageFromPackageDefinition(
                                packageDef,
                                context
                        );
                    } catch (Exception ex) {
                        context.logs().add("Failed to stage package definition: " + ex.getMessage());
                        context.errors().add(ex);
                    }
                }

            }

            // Check if any paths were staged successfully
            if (context.elementPaths().isEmpty()) {
                final var message = "No elements staged successfully for deployment " + deploymentId;
                context.logs().add("ERROR: " + message);
                throw new IllegalStateException(message);
            }

            context.logs().add("Staged " + context.elementPaths().size() + " element path(s)");

            // PHASE B: LOADING
            // Load all elements in a single operation
            context.logs().add("=== Phase B: Loading all elements ===");

            final List<Element> allElements;

            try {
                // Build API classloader from all element paths
                context.logs().add("Building API classloader from all element paths");

                final var permittedTypesClassloader = new PermittedTypesClassLoader();
                final var unconsumedSpiPaths = new HashSet<>(context.spiPaths().keySet());
                final var unconsumedAttributePaths = new HashSet<>(context.attributePaths().keySet());

                // Load all elements using LoadConfiguration
                context.logs().add("Loading elements from " + context.elementPaths().size() + " path(s)");
                final var config = ElementPathLoader.LoadConfiguration.builder()
                        .parent(permittedTypesClassloader)
                        .registry(context.registry())
                        .paths(context.elementPaths())
                        .spiProvider((parent, elementPath) -> {

                            final var result = context.createSpiClassLoaderFor(parent, elementPath);

                            if (unconsumedSpiPaths.remove(elementPath)) {
                                context.logs().add("Applied attributes to element at path: %s:%s".formatted(
                                        elementPath.getFileSystem(),
                                        elementPath
                                ));
                            } else if (parent == result) {
                                context.logs().add("Using default SPI for path: %s:%s".formatted(
                                        elementPath.getFileSystem(),
                                        elementPath
                                ));
                            } else {
                                context.warnings().add("Previously consumed SPI classpath to element at path %s:%s ".formatted(
                                        elementPath.getFileSystem(),
                                        elementPath
                                ));
                            }

                            return result;

                        })
                        .attributesLoader((baseAttrs, elementPath) -> {

                            final var finalAttributes =  context.createAttributesForPath(baseAttrs, elementPath);

                            if (unconsumedAttributePaths.remove(elementPath)) {
                                context.logs().add("Applied attributes to element at path: %s:%s\n%s".formatted(
                                        elementPath.getFileSystem(),
                                        elementPath,
                                        String.join(" -> \n", finalAttributes.getAttributeNames())
                                ));
                            } else {
                                context.warnings().add("Previously consumed attributes to element at path %s:%s ".formatted(
                                        elementPath.getFileSystem(),
                                        elementPath
                                ));
                            }

                            return finalAttributes;

                        })
                        .build();

                allElements = pathLoader.load(config).toList();

                if (!unconsumedSpiPaths.isEmpty()) {
                    context.warnings().add("Unconsumed SPI paths from element at path:\n%s".formatted(
                            unconsumedAttributePaths
                                    .stream()
                                    .map(p -> " -> %s:%s".formatted(p.getFileSystem(), p))
                                    .collect(Collectors.joining(", "))
                    ));
                }

                if (!unconsumedAttributePaths.isEmpty()) {
                    context.warnings().add("Unconsumed attributes from element at path:\n%s".formatted(
                            unconsumedAttributePaths
                                    .stream()
                                    .map(p -> " -> %s:%s".formatted(p.getFileSystem(), p))
                                    .collect(Collectors.joining(", "))
                    ));
                }

                context.logs().add("Successfully loaded " + allElements.size() + " element(s)");

            } catch (Exception ex) {
                context.logs().add("Failed to load elements: " + ex.getMessage());
                context.errors().add(ex);
                throw ex;
            }

            // PHASE C: STATUS DETERMINATION
            // Status was already determined at doLoadDeployment level based on errors/warnings
            // Just return the elements
            if (allElements.isEmpty()) {
                final var message = "Deployment " + deploymentId + " produced no elements";
                context.logs().add("ERROR: " + message);
                throw new IllegalStateException(message);
            }

            return allElements;

        } catch (Exception ex) {
            context.errors().add(ex);
            throw ex;
        }
    }

    /**
     * Checks if the deployment has an ELM file uploaded to LargeObject.
     */
    private boolean hasElmFromLargeObject(final ElementDeployment deployment) {
        return deployment.elm() != null && LargeObjectState.UPLOADED.equals(deployment.elm().getState());
    }

    /**
     * Stages elements from an ElementPackageDefinition.
     * Resolves the elmArtifact from Maven and validates it as an ELM file.
     * Does not load the elements - only prepares the path for loading.
     *
     * @param definition   the element package definition
     * @param context      the deployment context containing registry, temp files, file systems, logs, warnings, errors,
     *                     element paths, SPI paths, and attribute paths
     */
    private void stageFromPackageDefinition(
            final ElementPackageDefinition definition,
            final DeploymentContext context) {
        try {

            context.logs().add("Staging from package ELM artifact: " + definition.elmArtifact());

            // Resolve the ELM artifact
            final var elmArtifact = elementArtifactLoader.getArtifact(context.repositories(), definition.elmArtifact());

            final var elmPath = elmArtifact
                    .path()
                    .toAbsolutePath();

            context.logs().add("Resolved package ELM to: " + elmPath);

            // Validate the ELM file
            validateElmFile(elmPath, context.logs(), context.errors());

            final var fileSystem = FileSystems.newFileSystem(elmPath);
            context.fileSystems().add(fileSystem);

            final var fileSystemRoot = fileSystem
                    .getRootDirectories()
                    .iterator()
                    .next()
                    .toAbsolutePath();

            context.elementPaths().add(fileSystemRoot);

            if (definition.pathSpiClassPaths() != null) {
                definition.pathSpiClassPaths().forEach((path, classPath) -> {

                    final var fileSystemPath = fileSystem
                            .getPath(path)
                            .toAbsolutePath();

                    context.spiPaths().put(fileSystemPath, classPath);

                });
            }

            if (definition.pathAttributes() != null) {
                definition.pathAttributes().forEach((path, attributesMap) -> {

                    final var attributes = new SimpleAttributes.Builder()
                            .setAttributes(attributesMap)
                            .build();

                    final var fileSystemPath = fileSystem
                            .getPath(path)
                            .toAbsolutePath();

                    context.attributePaths().put(fileSystemPath, attributes);

                });
            }

            context.logs().add("Successfully staged package ELM");

        } catch (Exception ex) {
            context.logs().add("Failed to stage from ElementPackageDefinition: " + ex.getMessage());
            context.errors().add(ex);
        }
    }

    /**
     * Stages elements from an ElementPathDefinition.
     * Creates a directory structure and copies Maven artifacts (api, spi, element) into subdirectories.
     * Does not load the elements - only prepares the path for loading.
     *
     * @param definition    the element path definition
     * @param deploymentDir the deployment directory root
     * @param context       the deployment context containing registry, temp files, file systems, logs, warnings, errors,
     *                      element paths, SPI paths, and attribute paths
     */
    private void stageFromElementDefinition(final ElementPathDefinition definition,
                                            final Path deploymentDir,
                                            final DeploymentContext context) {
        try {

            context.logs().add("Staging from Maven artifacts");

            // Generate unique subdirectory name
            final var elementPath = deploymentDir
                    .resolve(definition.path())
                    .toAbsolutePath();

            createDirectories(elementPath);

            context.logs().add("Created element directory: " + elementPath);

            // Create subdirectories for artifacts
            final var apiDir = elementPath.resolve(ElementPathLoader.API_DIR);
            final var spiDir = elementPath.resolve(ElementPathLoader.SPI_DIR);
            final var libDir = elementPath.resolve(ElementPathLoader.LIB_DIR);

            createDirectories(apiDir);
            createDirectories(spiDir);
            createDirectories(libDir);

            // Gather and place API artifacts
            if (definition.apiArtifacts() != null && !definition.apiArtifacts().isEmpty()) {

                context.logs().add("Gathering " + definition.apiArtifacts().size() + " API artifact(s)");

                for (final var coordinate : definition.apiArtifacts()) {
                    context.copyArtifactWithDependencies(coordinate, apiDir);
                }

            }

            // Gather and place SPI artifacts
            if (definition.spiArtifacts() != null && !definition.spiArtifacts().isEmpty()) {

                context.logs().add("Gathering " + definition.spiArtifacts().size() + " SPI artifact(s)");

                for (final var coordinate : definition.spiArtifacts()) {
                    context.copyArtifactWithDependencies(coordinate, spiDir);
                }

            }

            // Gather and place implementation artifacts
            if (definition.elementArtifacts() != null && !definition.elementArtifacts().isEmpty()) {

                context.logs().add("Gathering " + definition.elementArtifacts().size() + " element artifact(s)");

                for (final var coordinate : definition.elementArtifacts()) {
                    context.copyArtifactWithDependencies(coordinate, libDir);
                }

            }

            // In a path based configuration we can just drop the properties file to disk
            // in the appropriate place on disk and let it be read later by the element path loader

            final var attributes = new SimpleAttributes.Builder()
                    .setAttributes(definition.attributes())
                    .build();

            // Add to element paths for later loading
            context.attributePaths().put(elementPath, attributes);

            context.logs().add("Successfully staged from Maven artifacts");

        } catch (Exception ex) {
            context.logs().add("Failed to stage from ElementDefinition: " + ex.getMessage());
            context.errors().add(ex);
        }
    }

    /**
     * Stages an ELM file from LargeObject by downloading it to a temp location.
     * Does not load the elements - only prepares the path for loading.
     *
     * @param deployment the deployment containing the LargeObject reference
     * @param context    the deployment context containing registry, temp files, file systems, logs, warnings, errors,
     *                   element paths, SPI paths, and attribute paths
     */
    private void stageFromLargeObject(final ElementDeployment deployment,
                                      final DeploymentContext context) {
        try {

            final var elmRef = deployment.elm();
            final var elmId = elmRef.getId();

            context.logs().add("Downloading ELM file from LargeObject: " + elmId);

            // Download ELM to temp file
            final var tempPath = temporaryFiles.createTempFile(deployment.id(), ".elm");
            context.tempFiles().add(tempPath);

            try (final InputStream in = getLargeObjectBucket().readObject(elmId);
                 final OutputStream out = Files.newOutputStream(tempPath)) {
                in.transferTo(out);
            }

            context.logs().add("Downloaded ELM to temporary file: " + tempPath);

            // Validate the ELM file
            validateElmFile(tempPath, context.logs(), context.errors());

            final var fileSystem = FileSystems.newFileSystem(tempPath);
            context.fileSystems().add(fileSystem);

            // Add to element paths for later loading
            final var fileSystemRoot = fileSystem
                    .getRootDirectories()
                    .iterator()
                    .next()
                    .toAbsolutePath();

            context.elementPaths().add(fileSystemRoot);
            context.logs().add("Successfully staged ELM from LargeObject.");

            if (deployment.pathSpiClassPaths() != null) {
                deployment.pathSpiClassPaths().forEach((path, classPath) -> {

                    final var fileSystemPath = fileSystem
                            .getPath(path)
                            .toAbsolutePath();

                    context.spiPaths().put(fileSystemPath, classPath);

                });
            }

            // Gather the paths for the attributes
            if (deployment.pathAttributes() != null) {
                deployment.pathAttributes().forEach((path, attributesMap) -> {

                    final var attributes = new SimpleAttributes.Builder()
                            .setAttributes(attributesMap)
                            .build();

                    final var fileSystemPath = fileSystem
                            .getPath(path)
                            .toAbsolutePath();

                    context.attributePaths().put(fileSystemPath, attributes);

                });
            }

        } catch (Exception ex) {
            context.logs().add("Failed to stage from LargeObject: " + ex.getMessage());
            context.errors().add(ex);
        }
    }

    /**
     * Validates that a file is a proper ELM file.
     * Checks both the file extension and ZIP format validity.
     *
     * @param elmPath the path to validate
     * @param logs the list to append log messages to
     * @param errors the list to append errors to
     * @throws IllegalArgumentException if validation fails
     */
    private void validateElmFile(final Path elmPath, final List<String> logs, final List<Throwable> errors) {

        // Check extension
        if (!elmPath.toString().endsWith(".elm")) {
            final var msg = "Artifact is not an ELM file: " + elmPath;
            logs.add(msg);
            final var ex = new InternalException(msg);
            errors.add(ex);
            throw ex;
        }

        // Verify it's a valid ZIP
        try (final var zis = new ZipInputStream(Files.newInputStream(elmPath))) {
            if (zis.getNextEntry() == null) {
                throw new InternalException("ELM file is empty or corrupted");
            }
        } catch (IOException ex) {
            final var msg = "ELM file is not a valid ZIP: " + elmPath;
            logs.add("ERROR: " + msg);
            final var wrappedEx = new InternalException(msg, ex);
            errors.add(wrappedEx);
            throw wrappedEx;
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

    /**
     * Publishes runtime service started event.
     */
    private void publishRuntimeServiceStarted() {
        try {
            final var event = Event.builder()
                    .named(ElementRuntimeService.RUNTIME_SERVICE_STARTED)
                    .build();
            getRootElementRegistry().publish(event);
            logger.debug("Published runtime service started event");
        } catch (Exception ex) {
            logger.error("Failed to publish runtime service started event", ex);
        }
    }

    /**
     * Publishes runtime service stopped event.
     */
    private void publishRuntimeServiceStopped() {
        try {
            final var event = Event.builder()
                    .named(ElementRuntimeService.RUNTIME_SERVICE_STOPPED)
                    .build();
            getRootElementRegistry().publish(event);
            logger.debug("Published runtime service stopped event");
        } catch (Exception ex) {
            logger.error("Failed to publish runtime service stopped event", ex);
        }
    }

    /**
     * Publishes runtime loaded event with the runtime record.
     */
    private void publishRuntimeLoaded(final RuntimeRecord record) {
        try {
            final var event = Event.builder()
                    .named(ElementRuntimeService.RUNTIME_LOADED)
                    .argument(record)
                    .build();
            getRootElementRegistry().publish(event);
            logger.debug("Published runtime loaded event for deployment: {}", record.deployment().id());
        } catch (Exception ex) {
            logger.error("Failed to publish runtime loaded event for deployment: {}",
                    record.deployment().id(), ex);
        }
    }

    /**
     * Publishes runtime unloaded event with the deployment ID.
     */
    private void publishRuntimeUnloaded(final String deploymentId) {
        try {
            final var event = Event.builder()
                    .named(ElementRuntimeService.RUNTIME_UNLOADED)
                    .argument(deploymentId)
                    .build();
            getRootElementRegistry().publish(event);
            logger.debug("Published runtime unloaded event for deployment: {}", deploymentId);
        } catch (Exception ex) {
            logger.error("Failed to publish runtime unloaded event for deployment: {}", deploymentId, ex);
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

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(final ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    /**
     * Called by the event handler to reconcile a newly created deployment.
     *
     * @param elementDeployment the deployment
     */
    @ElementEventConsumer(
            value = ElementDeploymentDao.ELEMENT_DEPLOYMENT_CREATED,
            via = @ElementServiceReference(ElementRuntimeService.class)
    )
    public void onDeploymentCreated(final ElementDeployment elementDeployment) {
        logger.info("Element deployment created: {}. Reconciling.", elementDeployment.id());
        safeReconcile();
    }

    /**
     * Called by the event handler to reconcile a newly updated deployment.
     *
     * @param elementDeployment the deployment
     */
    @ElementEventConsumer(
            value = ElementDeploymentDao.ELEMENT_DEPLOYMENT_UPDATED,
            via = @ElementServiceReference(ElementRuntimeService.class)
    )
    public void onDeploymentUpdated(final ElementDeployment elementDeployment) {
        logger.info("Element deployment updated: {}. Reconciling.", elementDeployment.id());
        safeReconcile();
    }

    /**
     * Called by the event handler to reconcile a recently deleted deployment.
     *
     * @param elementDeployment the deployment
     */
    @ElementEventConsumer(
            value = ElementDeploymentDao.ELEMENT_DEPLOYMENT_DELETED,
            via = @ElementServiceReference(ElementRuntimeService.class)
    )
    public void onDeploymentDeleted(final ElementDeployment elementDeployment) {
        logger.info("Element deployment deleted: {}. Reconciling.", elementDeployment.id());
        safeReconcile();
    }

    /**
     * Encapsulates the mutable context used during element deployment operations.
     * This record groups together the registry and various tracking lists that are
     * passed through the deployment pipeline.
     */
    private record DeploymentContext(
            ElementDeployment deployment,
            MutableElementRegistry registry,
            List<Path> tempFiles,
            List<FileSystem> fileSystems,
            List<String> logs,
            List<String> warnings,
            List<Throwable> errors,
            List<Path> elementPaths,
            Map<Path, List<String>> spiPaths,
            Map<Path, Attributes> attributePaths,
            ElementArtifactLoader artifactLoader,
            Set<ArtifactRepository> repositories,
            TemporaryFiles temporaryFiles
    ) {

        public DeploymentContext {
            requireNonNull(deployment, "deployment");
            repositories = repositories == null ? new HashSet<>() : repositories;
            buildRepositories(deployment, repositories);
        }

        private static void buildRepositories(
                final ElementDeployment deployment,
                final Set<ArtifactRepository> repositories) {

            // Add default repositories if requested
            if (deployment.useDefaultRepositories()) {
                repositories.addAll(ArtifactRepository.DEFAULTS);
            }

            // Add explicit repositories
            if (deployment.repositories() != null) {
                deployment.repositories()
                        .stream()
                        .map(ear -> new ArtifactRepository(ear.id(), ear.url()))
                        .forEach(repositories::add);
            }

        }

        /**
         * Creates a custom SPI classloader for the given element path if custom SPI dependencies are configured.
         * If no custom SPI is configured, returns the parent classloader.
         *
         * @param parent           the parent classloader
         * @param elementPath      the element path to create SPI classloader for
         * @return a classloader with the SPI dependencies, or the parent if no custom SPI
         * @throws IOException if artifact resolution or file operations fail
         */
        public ClassLoader createSpiClassLoaderFor(final ClassLoader parent, final Path elementPath) {

            final var spiClassPath = spiPaths.get(elementPath);

            if (spiClassPath == null) {
                logs.add("%s uses default SPI. Not loading SPI".formatted(elementPath));
                return parent;
            }

            final var spiTarget = temporaryFiles.createTempDirectory("spi");
            tempFiles.add(spiTarget);

            for (final var coordinates : spiClassPath) {
                try {
                    copyArtifactWithDependencies(coordinates, spiTarget);
                } catch (IOException e) {
                    warnings.add(
                            "Caught IO Exception assembling classpath %s"
                                    .formatted(e.getMessage())
                    );
                    errors.add(e);
                }
            }

            // Collect all JAR files in the spiTarget directory for the classloader
            URL[] jarUrls;

            try (final var pathStream = Files.walk(spiTarget, 1)) {
                jarUrls = pathStream
                        .filter(p -> p.toString().endsWith(".jar"))
                        .map(p -> {
                            try {
                                return p.toUri().toURL();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toArray(URL[]::new);
            } catch (IOException e) {
                jarUrls = new URL[0];
                warnings.add("Caught IO Exception assembling classpath %s".formatted(e.getMessage()));
                errors.add(e);
            }

            if (jarUrls.length == 0) {
                warnings.add("No JAR files found in SPI directory for path: " + elementPath);
            }

            return new URLClassLoader(jarUrls, parent);

        }

        /**
         * Copies a Maven artifact and its transitive dependencies to a target directory.
         * Logs the resolution and copying process to the deployment logs.
         *
         * @param coordinates the Maven coordinates (e.g., "groupId:artifactId:version")
         * @param targetDir   the target directory to copy artifacts into
         * @throws IOException if artifact resolution or file copying fails
         */
        public void copyArtifactWithDependencies(
                final String coordinates,
                final Path targetDir
        ) throws IOException {

            logs.add("Resolving artifact with dependencies: " + coordinates);

            final var artifacts = artifactLoader.findClasspathForArtifact(repositories, coordinates).toList();
            logs.add("Found %d artifact(s) including dependencies %s".formatted(artifacts.size(), coordinates));

            for (final var artifact : artifacts) {

                final var sourcePath = artifact.path();
                final var fileName = "%s.%s.%s.%s".formatted(
                        artifact.group(),
                        artifact.id(),
                        artifact.version(),
                        artifact.extension()
                );

                final var destinationPath = targetDir.resolve(fileName);
                copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                logs.add("Copied artifact: " + fileName);

            }

        }

        public Attributes createAttributesForPath(final Attributes baseAttrs, final Path elementPath) {

            // Look up pre-computed attributes for this path
            final var resolved = attributePaths().getOrDefault(
                    elementPath.toAbsolutePath(),
                    Attributes.emptyAttributes()
            );

            // Create the base attributes found in the deployment
            return new SimpleAttributes.Builder()
                    .from(baseAttrs)
                    .from(resolved)
                    .build();

        }

    }

    /**
     * Tracks an active deployment's runtime state.
     */
    private record ActiveDeployment(
            ElementDeployment deployment,
            RuntimeStatus status,
            boolean isTransient,
            MutableElementRegistry registry,
            List<Element> elements,
            List<Path> tempFiles,
            List<FileSystem> filesystems,
            List<String> logs,
            List<Throwable> errors
    ) implements AutoCloseable {

        /**
         * Converts this active deployment to a RuntimeRecord for external visibility.
         * RuntimeRecord is a snapshot used for events and API responses.
         *
         * @return a RuntimeRecord representing this deployment's current state
         */
        public RuntimeRecord toRuntimeRecord() {
            return new RuntimeRecord(
                    deployment,
                    status,
                    isTransient,
                    registry,
                    elements,
                    tempFiles,
                    logs,
                    errors
            );
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

            for (final FileSystem fileSystem : filesystems) {
                try {
                    fileSystem.close();
                } catch (IOException ex) {
                    logger.warn("Failed to close file system {} for deployment {}", fileSystem, deployment.id(), ex);
                }
            }
        }
    }

}

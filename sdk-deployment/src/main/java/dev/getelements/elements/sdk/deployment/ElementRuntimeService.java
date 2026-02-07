package dev.getelements.elements.sdk.deployment;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.system.ElementArtifactRepository;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementPackageDefinition;
import dev.getelements.elements.sdk.model.system.ElementPathDefinition;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Service that polls the database for {@link dev.getelements.elements.sdk.model.system.ElementDeployment} records
 * and manages the runtime lifecycle of Element plugins. Each deployment gets its own subordinate
 * {@link dev.getelements.elements.sdk.MutableElementRegistry}. The service reconciles database state with
 * in-memory state on a configurable interval: loading ENABLED deployments, unloading DISABLED/deleted ones.
 *
 * <h3>Runtime Lifecycle Events</h3>
 * The service publishes the following events:
 * <ul>
 *     <li>{@link #RUNTIME_SERVICE_STARTED} - when the service starts</li>
 *     <li>{@link #RUNTIME_SERVICE_STOPPED} - when the service stops</li>
 *     <li>{@link #RUNTIME_LOADED} - when a runtime deployment is loaded</li>
 *     <li>{@link #RUNTIME_UNLOADED} - when a runtime deployment is unloaded</li>
 * </ul>
 */
@ElementPublic
@ElementEventProducer(
        value = ElementRuntimeService.RUNTIME_SERVICE_STARTED,
        description = "Published when the runtime service is started."
)
@ElementEventProducer(
        value = ElementRuntimeService.RUNTIME_SERVICE_STOPPED,
        description = "Published when the runtime service is stopped."
)
@ElementEventProducer(
        value = ElementRuntimeService.RUNTIME_LOADED,
        parameters = {String.class, ElementRuntimeService.RuntimeStatus.class, Boolean.class, ElementRuntimeService.RuntimeRecord.class},
        description = "Published when a runtime deployment is loaded. Arguments: deploymentId, status, isTransient, record"
)
@ElementEventProducer(
        value = ElementRuntimeService.RUNTIME_UNLOADED,
        parameters = String.class,
        description = "Published when a runtime deployment is unloaded. Arguments: deploymentId"
)
public interface ElementRuntimeService {

    /**
     * Event published when the runtime service is started.
     */
    String RUNTIME_SERVICE_STARTED = "dev.getelements.elements.runtime.service.started";

    /**
     * Event published when the runtime service is stopped.
     */
    String RUNTIME_SERVICE_STOPPED = "dev.getelements.elements.runtime.service.stopped";

    /**
     * Event published when a runtime deployment is loaded.
     * Event arguments: deploymentId (String), status (RuntimeStatus), isTransient (Boolean), record (RuntimeRecord)
     */
    String RUNTIME_LOADED = "dev.getelements.elements.runtime.loaded";

    /**
     * Event published when a runtime deployment is unloaded.
     * Event arguments: deploymentId (String)
     */
    String RUNTIME_UNLOADED = "dev.getelements.elements.runtime.unloaded";

    /**
     * The attribute key for the poll interval in seconds. Default is 30 seconds.
     */
    @ElementDefaultAttribute(value = "30", description = "Poll interval in seconds for Element deployment changes.")
    String POLL_INTERVAL_SECONDS = "dev.getelements.elements.runtime.poll.interval.seconds";

    /**
     * Starts the runtime service. This begins the polling loop that reconciles Element deployments.
     */
    void start();

    /**
     * Stops the runtime service. This stops the polling loop and unloads all active deployments.
     */
    void stop();

    /**
     * Gets all active {@link ElementDeployment} instances as a copy or snapshot of the internal state of the
     * deployments. This includes both persistent (database-backed) and transient deployments.
     *
     * @return all active deployments.
     */
    List<RuntimeRecord> getActiveRuntimes();

    /**
     * Unloads a transient deployment by ID.
     *
     * @param deploymentId the deployment ID to unload
     * @return true if the deployment was found and unloaded, false if not found or not transient
     * @throws IllegalStateException if the service is not started
     */
    boolean unloadTransientDeployment(String deploymentId);

    /**
     * Loads a transient deployment from Maven coordinates for testing and development.
     * The service assigns a unique ID and loads the deployment immediately without persisting to the database.
     * Transient deployments are excluded from database reconciliation and remain loaded until explicitly
     * unloaded or the service is stopped.
     *
     * @param request the Maven-based deployment configuration
     * @return the runtime record for the loaded deployment
     * @throws IllegalStateException if the service is not started
     */
    RuntimeRecord loadTransientDeployment(TransientDeploymentRequest request);

    /**
     * Configuration for loading a transient deployment from Maven artifact coordinates.
     * The service will assign an ID and manage the deployment lifecycle.
     *
     * @param application the application context (null for system-wide deployment)
     * @param pathAttributes custom attributes per element path (may be null)
     * @param elements list of path-based element definitions (may be null)
     * @param packages list of package-based element definitions (may be null)
     * @param useDefaultRepositories whether to include default Maven repositories
     * @param repositories additional artifact repositories for resolution (may be empty)
     */
    record TransientDeploymentRequest(
            Application application,
            Map<String, List<String>> pathSpiClasspath,
            Map<String, Map<String, Object>> pathAttributes,
            List<ElementPathDefinition> elements,
            List<ElementPackageDefinition> packages,
            boolean useDefaultRepositories,
            List<ElementArtifactRepository> repositories
    ) {

        /**
         * Canonical constructor that ensures all collections are immutable copies.
         */
        public TransientDeploymentRequest {
            // Create immutable copies of path attributes with nested maps

            if (pathSpiClasspath != null) {
                pathSpiClasspath = pathSpiClasspath.entrySet().stream()
                        .collect(java.util.stream.Collectors.toUnmodifiableMap(
                                java.util.Map.Entry::getKey,
                                entry -> entry.getValue() == null
                                        ? java.util.List.of()
                                        : java.util.List.copyOf(entry.getValue())
                        ));
            }

            if (pathAttributes != null) {
                pathAttributes = pathAttributes.entrySet().stream()
                        .collect(java.util.stream.Collectors.toUnmodifiableMap(
                                java.util.Map.Entry::getKey,
                                entry -> entry.getValue() == null
                                        ? java.util.Map.of()
                                        : java.util.Map.copyOf(entry.getValue())
                        ));
            }

            // Create immutable copies of lists
            elements = elements == null ? null : java.util.List.copyOf(elements);
            packages = packages == null ? null : java.util.List.copyOf(packages);
            repositories = repositories == null ? null : java.util.List.copyOf(repositories);

        }

        /**
         * Creates a new builder for TransientDeploymentRequest.
         *
         * @return a new builder instance
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Builder for TransientDeploymentRequest.
         */
        public static final class Builder {
            private Application application;
            private Map<String, List<String>> pathSpiClasspath;
            private Map<String, Map<String, Object>> pathAttributes;
            private List<ElementPathDefinition> elements;
            private List<ElementPackageDefinition> packages;
            private boolean useDefaultRepositories = true;
            private List<ElementArtifactRepository> repositories;

            private Builder() {
            }

            /**
             * Sets the application context.
             *
             * @param application the application context (null for system-wide deployment)
             * @return this builder
             */
            public Builder application(final Application application) {
                this.application = application;
                return this;
            }

            /**
             * Sets the path attributes map.
             *
             * @param pathAttributes custom attributes per element path
             * @return this builder
             */
            public Builder pathAttributes(final Map<String, Map<String, Object>> pathAttributes) {
                this.pathAttributes = pathAttributes;
                return this;
            }

            /**
             * Adds attributes for a specific element path.
             *
             * @param path the element path
             * @param attributes the attributes to add for this path
             * @return this builder
             */
            public Builder addPathAttributes(final String path, final Map<String, Object> attributes) {
                if (this.pathAttributes == null) {
                    this.pathAttributes = new java.util.HashMap<>();
                }
                this.pathAttributes.put(path, attributes);
                return this;
            }

            /**
             * Sets the SPI Classpath for a specific path within the deployment.
             *
             * @param path the path
             * @param classpath the classpath
             * @return this instance
             */
            public Builder addPathSpiClasspath(final String path, final List<String> classpath) {

                if (this.pathSpiClasspath == null) {
                    this.pathSpiClasspath = new java.util.HashMap<>();
                }

                this.pathSpiClasspath.put(path, classpath);
                return this;
            }

            /**
             * Adds a single attribute for a specific element path.
             *
             * @param path the element path
             * @param key the attribute key
             * @param value the attribute value
             * @return this builder
             */
            public Builder addPathAttribute(final String path, final String key, final Object value) {
                if (this.pathAttributes == null) {
                    this.pathAttributes = new java.util.HashMap<>();
                }
                this.pathAttributes
                        .computeIfAbsent(path, k -> new java.util.HashMap<>())
                        .put(key, value);
                return this;
            }

            /**
             * Sets the list of path-based element definitions.
             *
             * @param elements list of path-based element definitions
             * @return this builder
             */
            public Builder elements(final List<ElementPathDefinition> elements) {
                this.elements = elements;
                return this;
            }

            /**
             * Adds a single path-based element definition.
             *
             * @param element the element definition to add
             * @return this builder
             */
            public Builder addElement(final ElementPathDefinition element) {
                if (this.elements == null) {
                    this.elements = new java.util.ArrayList<>();
                }
                this.elements.add(element);
                return this;
            }

            /**
             * Sets the list of package-based element definitions.
             *
             * @param packages list of package-based element definitions
             * @return this builder
             */
            public Builder packages(final List<ElementPackageDefinition> packages) {
                this.packages = packages;
                return this;
            }

            /**
             * Adds a single package-based element definition.
             *
             * @param packageDef the package definition to add
             * @return this builder
             */
            public Builder addPackage(final ElementPackageDefinition packageDef) {
                if (this.packages == null) {
                    this.packages = new java.util.ArrayList<>();
                }
                this.packages.add(packageDef);
                return this;
            }

            /**
             * Sets whether to use default Maven repositories.
             *
             * @param useDefaultRepositories true to include default repositories
             * @return this builder
             */
            public Builder useDefaultRepositories(final boolean useDefaultRepositories) {
                this.useDefaultRepositories = useDefaultRepositories;
                return this;
            }

            /**
             * Sets the list of artifact repositories.
             *
             * @param repositories list of artifact repositories
             * @return this builder
             */
            public Builder repositories(final List<ElementArtifactRepository> repositories) {
                this.repositories = repositories;
                return this;
            }

            /**
             * Adds a single artifact repository.
             *
             * @param repository the artifact repository to add
             * @return this builder
             */
            public Builder addRepository(final ElementArtifactRepository repository) {
                if (this.repositories == null) {
                    this.repositories = new java.util.ArrayList<>();
                }
                this.repositories.add(repository);
                return this;
            }

            /**
             * Builds the TransientDeploymentRequest.
             *
             * @return a new TransientDeploymentRequest instance
             */
            public TransientDeploymentRequest build() {
                return new TransientDeploymentRequest(
                        application,
                        pathSpiClasspath,
                        pathAttributes,
                        elements,
                        packages,
                        useDefaultRepositories,
                        repositories
                );
            }
        }
    }

    /**
     * Represents an active element runtime.
     *
     * @param deployment the underlying {@link ElementDeployment}
     * @param status the runtime status indicating load success or failure
     * @param isTransient true if this is a transient (non-persistent) deployment
     * @param registry the {@link ElementRegistry} used to manage the Elements
     * @param elements the {@link Element}s loaded in this deployment
     * @param tempFiles temporary files created during deployment loading
     * @param logs log messages from the loading process
     * @param errors errors encountered during loading
     */
    record RuntimeRecord(
            ElementDeployment deployment,
            RuntimeStatus status,
            boolean isTransient,
            ElementRegistry registry,
            List<Element> elements,
            List<Path> tempFiles,
            List<String> logs,
            List<Throwable> errors
    ) {}

    /**
     * Indicates the runtime status.
     */
    enum RuntimeStatus {

        /**
         * The runtime loaded successfully.
         */
        CLEAN,

        /**
         * Indicates that the runtime loaded successfully and there were warnings associated with the process.
         */
        WARNINGS,

        /**
         * Indicates that there were exceptions, which were caught, in the loading process. At least one Element
         * failed to properly load.
         */
        UNSTABLE,

        /**
         * The deployment has failed due to one reason or another due to the inability to load the application or
         * its elements.
         */
        FAILED

    }

}

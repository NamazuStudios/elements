package dev.getelements.elements.sdk.deployment;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
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
            Map<String, Map<String, Object>> pathAttributes,
            List<ElementPathDefinition> elements,
            List<ElementPackageDefinition> packages,
            boolean useDefaultRepositories,
            List<ElementArtifactRepository> repositories
    ) {}

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

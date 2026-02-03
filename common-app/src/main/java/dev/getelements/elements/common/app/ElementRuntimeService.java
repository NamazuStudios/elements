package dev.getelements.elements.common.app;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.model.system.ElementDeployment;

import java.util.List;

/**
 * Service that polls the database for {@link dev.getelements.elements.sdk.model.system.ElementDeployment} records
 * and manages the runtime lifecycle of Element plugins. Each deployment gets its own subordinate
 * {@link dev.getelements.elements.sdk.MutableElementRegistry}. The service reconciles database state with
 * in-memory state on a configurable interval: loading ENABLED deployments, unloading DISABLED/deleted ones.
 */
public interface ElementRuntimeService {

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
     * deployments.
     *
     * @return all active deployments.
     */
    List<RuntimeRecord> getActiveDeployments();

    /**
     * Represents an active element runtime.
     *
     * @param deployment the underlying {@link ElementDeployment}
     * @param registry the {@link ElementRegistry} used to manage the Elements
     * @param elements the {@link Element}s loaded in this deployment
     */
    record RuntimeRecord(
            ElementDeployment deployment,
            RuntimeStatus status,
            ElementRegistry registry,
            List<Element> elements,
            List<String> logs,
            List<Throwable> errors
    ) {

        /**
         * Returns the application id, if an application is present.
         * @return the application id or null
         */
        public String applicationId() {
            return deployment().application() == null ? null : deployment().application().getId();
        }

        /**
         * Returns the application name, if an application is present.
         *
         * @return the application name or null
         */
        public String applicationName() {
            return deployment().application() == null ? null : deployment().application().getName();
        }

    }

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

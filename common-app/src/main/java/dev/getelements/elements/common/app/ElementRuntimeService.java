package dev.getelements.elements.common.app;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;

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

}

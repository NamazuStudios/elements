package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.exception.MultiException;
import dev.getelements.elements.sdk.cluster.id.InstanceId;

import java.util.concurrent.ExecutorService;

/**
 * Represents a running {@link Instance} of the application.  Though not strictly required, there ought only be one
 * {@link Instance} per machine or process space.  The rationale is that the {@link Instance} is dedicated to making
 * full use of the machine's horsepower to perform operations.
 */
public interface Instance extends AutoCloseable {

    /**
     * The worker thread group.
     */
    String THREAD_GROUP = "dev.getelements.elements.rt.thread.group";

    /**
     * Used with {@link jakarta.inject.Named} to name an instance of {@link ExecutorService} which is a general purpose
     * pool of threads used for performing various tasks within the system.
     */
    String EXECUTOR_SERVICE = "dev.getelements.elements.rt.executor";
    /**
     * Used with {@link jakarta.inject.Named} to name an instance of {@link java.util.concurrent.ScheduledExecutorService}
     * which is a general purpose pool of threads used for performing various tasks within the system that require
     * scheduling.
     */
    String SCHEDULED_EXECUTOR_SERVICE = "dev.getelements.elements.rt.scheduled.executor";

    /**
     * Gets this instances {@link InstanceId}
     *
     * @return the {@link InstanceId}
     */
    InstanceId getInstanceId();

    /**
     * Attempts to start, throwing an instance of {@link MultiException} any failures happen during the startup process.
     * A subsequent call to {@link #close()} should follow to ensure resources are cleaned up.
     */
    void start();

    /**
     * Attempts to stop the instance.
     */
    @Override
    void close();

    /**
     * Forces a refresh of the underlying connections as necessary.
     */
    void refreshConnections();

}

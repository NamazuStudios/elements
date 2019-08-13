package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.id.InstanceId;

/**
 * Represents a running {@link Instance} of the application.  Though not strictly required, there ought only be one
 * {@link Instance} per machine or process space.  The rationale is that the {@link Instance} is dedicated to making
 * full use of the machine's horsepower to perform operations.
 */
public interface Instance extends AutoCloseable {

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

}

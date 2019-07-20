package com.namazustudios.socialengine.rt;

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
     * Attempts to start each {@link Node}, throwing an instance of {@link MultiException} if any {@link Node} fails
     * to startup.  A subsequent call to {@link #close()} should follow to ensure resources are cleaned up.
     */
    void start();

    /**
     * Attempts to stop the instance.
     */
    @Override
    void close();

}

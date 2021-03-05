package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.InvalidInstanceIdException;
import com.namazustudios.socialengine.rt.id.InstanceId;

import java.io.*;

/**
 * Drives the instance-wide persistence system, if available. Currently this is just allows for the start and stop
 * operations.
 */
public interface Persistence {

    /**
     * Starts the {@link Persistence} instance and obtains all resources necessary to begin accessing the
     * underlying data store.
     */
    void start();

    /**
     * Closes this {@link Persistence} instance and releases any underlying connections to
     * the data storage. Outstanding transactions may be forcibly closed if this is called.
     */
    void stop();

}

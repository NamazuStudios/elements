package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.util.function.Function;

/**
 * A system-wide service to house {@link AsyncConnection} instances.  Each instance of {@link AsyncConnection} is
 * managed internally and callbacks issued on a background thread dedicated to that as well as other {@link Connection}
 * instances.
 */
public interface AsyncConnectionService {

    /**
     * Starts the {@link AsyncConnectionService}.
     */
    void start();

    /**
     * Stops the {@link AsyncConnectionService}.
     */
    void stop();

    /**
     * Returns a {@link AsyncConnectionGroup.Builder} which can be used to build an instance of {@link AsyncConnectionGroup} for managing closely
     * related {@link AsyncConnection} instances.
     *
     * @return the {@link AsyncConnectionGroup.Builder}
     */
    AsyncConnectionGroup.Builder group();

    /**
     * Allocates an instance of {@link AsyncConnectionPool}.
     *
     * @param name
     * @param minConnections
     * @param maxConnextions
     * @param socketSupplier
     * @return
     */
    AsyncConnectionPool allocatePool(String name,
                                     int minConnections, int maxConnextions,
                                     Function<ZContext, Socket> socketSupplier);

}

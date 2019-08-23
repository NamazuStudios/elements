package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.Consumer;
import java.util.function.Function;

public interface AsyncConnectionService {


    void start();

    void stop();

    ManagedPool allocatePool(String name,
                             int minConnections, int maxConnextions,
                             Function<ZContext, ZMQ.Socket> socketSupplier);

    interface ManagedPool extends AutoCloseable {

        void acquireNextAvailableConnection(Consumer<AsyncConnection> asyncConnectionConsumer);

        void close();

    }

}

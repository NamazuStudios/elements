package com.namazustudios.socialengine.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.jeromq.ZContextProvider;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.IO_THREADS;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.MAX_SOCKETS;
import static java.lang.Runtime.getRuntime;

public class ZContextModule extends PrivateModule {

    private Runnable ioThreadsAction = () -> {};

    private Runnable maxSocketsAction = () -> {};

    @Override
    protected void configure() {
        ioThreadsAction.run();
        maxSocketsAction.run();
        bind(ZContext.class).toProvider(ZContextProvider.class).asEagerSingleton();
        expose(ZContext.class);
    }

    public ZContextModule withDefaultIoThreads() {
        final int ioThreads = getRuntime().availableProcessors() + 1;
        return withIoThreads(ioThreads);
    }

    public ZContextModule withIoThreads(final int ioThreads) {
        ioThreadsAction = () -> bind(Integer.class).annotatedWith(named(IO_THREADS)).toInstance(ioThreads);
        return this;
    }

    public ZContextModule withDefaultMaxSockets() {
        return withMaxSockets(zmq.ZMQ.ZMQ_MAX_SOCKETS_DFLT);
    }

    public ZContextModule withMaxSockets(int maxSockets) {
        maxSocketsAction = () -> bind(Integer.class).annotatedWith(named(MAX_SOCKETS)).toInstance(maxSockets);
        return this;
    }

}

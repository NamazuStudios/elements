package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.jeromq.JeroMQAsyncConnectionService;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQAsyncConnectionService.ASYNC_CONNECTION_IO_THREADS;
import static java.lang.Runtime.getRuntime;

public class JeroMQAsyncConnectionServiceModule extends PrivateModule {

    private Runnable bindIoThreads = () -> {};

    @Override
    protected void configure() {

        requireBinding(ZContext.class);

        bindIoThreads.run();
        bind(JeroMQAsyncConnectionService.class).asEagerSingleton();
        bind(new TypeLiteral<AsyncConnectionService<?, ?>>(){}).to(JeroMQAsyncConnectionService.class);
        bind(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){}).to(JeroMQAsyncConnectionService.class);

        expose(new TypeLiteral<AsyncConnectionService<?, ?>>(){});
        expose(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){});

    }

    public JeroMQAsyncConnectionServiceModule withDefaultIoThreads() {
        return withIoThreads(getRuntime().availableProcessors() + 1);
    }

    public JeroMQAsyncConnectionServiceModule withIoThreads(final int ioThreads) {
        bindIoThreads = () -> bind(Integer.class)
            .annotatedWith(named(ASYNC_CONNECTION_IO_THREADS))
            .toInstance(ioThreads);
        return this;
    }

}

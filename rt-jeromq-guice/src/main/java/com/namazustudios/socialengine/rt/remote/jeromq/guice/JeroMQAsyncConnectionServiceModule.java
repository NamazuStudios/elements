package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.jeromq.JeroMQAsyncConnectionService;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JeroMQAsyncConnectionServiceModule extends PrivateModule {

    @Override
    protected void configure() {

        requireBinding(ZContext.class);

        bind(JeroMQAsyncConnectionService.class).asEagerSingleton();
        bind(new TypeLiteral<AsyncConnectionService<?, ?>>(){}).to(JeroMQAsyncConnectionService.class);
        bind(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){}).to(JeroMQAsyncConnectionService.class);

        expose(new TypeLiteral<AsyncConnectionService<?, ?>>(){});
        expose(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){});

    }

}

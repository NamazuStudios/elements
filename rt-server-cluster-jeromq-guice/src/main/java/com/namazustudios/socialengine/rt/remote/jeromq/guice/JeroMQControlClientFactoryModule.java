package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.remote.AsyncControlClient;
import com.namazustudios.socialengine.rt.remote.ControlClient;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQAsyncControlClient;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlClient;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JeroMQControlClientFactoryModule extends PrivateModule {
    @Override
    protected void configure() {

        final var zContextProvider = getProvider(ZContext.class);
        bind(ControlClient.Factory.class).toInstance(ca -> new JeroMQControlClient(zContextProvider.get(), ca));

        final var key = Key.get(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){});
        final var asp = getProvider(key);
        bind(AsyncControlClient.Factory.class).toInstance(ca -> new JeroMQAsyncControlClient(asp.get(), ca));

        expose(ControlClient.Factory.class);
        expose(AsyncControlClient.Factory.class);

    }
}

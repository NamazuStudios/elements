package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.remote.AsyncControlClient;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQAsyncControlClient;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JeroMQAsyncControlClientModule extends PrivateModule {

    @Override
    protected void configure() {
        final var key = Key.get(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){});
        final var asp = getProvider(key);
        bind(AsyncControlClient.Factory.class).toInstance(ca -> new JeroMQAsyncControlClient(asp.get(), ca));
        expose(AsyncControlClient.Factory.class);
    }

}

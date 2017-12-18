package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQNode;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.jackson.guice.CBORJacksonModule;
import com.namazustudios.socialengine.rt.jeromq.CachedConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import org.zeromq.ZContext;

public class JeroMQNodeModule extends PrivateModule {

    @Override
    protected void configure() {

        install(new CBORJacksonModule());

        bind(ZContext.class).asEagerSingleton();
        bind(Node.class).to(JeroMQNode.class).asEagerSingleton();
        bind(ConnectionPool.class).to(CachedConnectionPool.class).asEagerSingleton();

        expose(Node.class);

    }

}

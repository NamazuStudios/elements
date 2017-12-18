package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker;
import com.namazustudios.socialengine.rt.jackson.guice.CBORJacksonModule;
import com.namazustudios.socialengine.rt.jeromq.CachedConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import org.zeromq.ZContext;

public class JeroMQRemoteInvokerModule extends PrivateModule {

    @Override
    protected void configure() {

        install(new CBORJacksonModule());

        bind(ZContext.class).asEagerSingleton();
        bind(RemoteInvoker.class).to(JeroMQRemoteInvoker.class).asEagerSingleton();
        bind(ConnectionPool.class).to(CachedConnectionPool.class).asEagerSingleton();

        expose(RemoteInvoker.class);

    }

}

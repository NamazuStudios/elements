package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionsManager;
import com.namazustudios.socialengine.remote.jeromq.srv.SpotifySrvMonitor;
import com.namazustudios.socialengine.remote.jeromq.srv.SrvMonitor;
import com.namazustudios.socialengine.rt.MultiplexedConnectionsManager;
import org.zeromq.ZContext;

public class JeroMQMultiplexerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ZContext.class).asEagerSingleton();
        bind(MultiplexedConnectionsManager.class).to(JeroMQMultiplexedConnectionsManager.class).asEagerSingleton();
        bind(SrvMonitor.class).to(SpotifySrvMonitor.class).asEagerSingleton();
    }

}

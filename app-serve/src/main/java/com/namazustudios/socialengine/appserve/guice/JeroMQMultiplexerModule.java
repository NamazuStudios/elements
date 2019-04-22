package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService;
import com.namazustudios.socialengine.rt.remote.srv.SpotifySrvMonitor;
import com.namazustudios.socialengine.rt.remote.srv.SrvMonitor;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import org.zeromq.ZContext;

public class JeroMQMultiplexerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ZContext.class).asEagerSingleton();
        bind(ConnectionService.class).to(JeroMQMultiplexedConnectionService.class).asEagerSingleton();
        bind(SrvMonitor.class).to(SpotifySrvMonitor.class).asEagerSingleton();
    }

}

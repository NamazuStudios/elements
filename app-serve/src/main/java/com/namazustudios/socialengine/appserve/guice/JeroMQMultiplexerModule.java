package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQInstanceMetadataContext;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.srv.SpotifySrvMonitorService;
import com.namazustudios.socialengine.rt.srv.SrvMonitorService;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import org.zeromq.ZContext;

public class JeroMQMultiplexerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ZContext.class).asEagerSingleton();
        bind(ConnectionService.class).to(JeroMQMultiplexedConnectionService.class).asEagerSingleton();
        bind(InstanceMetadataContext.class).to(JeroMQInstanceMetadataContext.class).asEagerSingleton();
        bind(SrvMonitorService.class).to(SpotifySrvMonitorService.class).asEagerSingleton();
    }

}

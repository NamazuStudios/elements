package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.remote.jeromq.JeroMQInstanceMetadataContext;
import com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService;
import com.namazustudios.socialengine.rt.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.SrvInstanceDiscoveryService;
import com.namazustudios.socialengine.rt.StaticInstanceDiscoveryService;
import com.namazustudios.socialengine.rt.srv.SpotifySrvMonitorService;
import com.namazustudios.socialengine.rt.srv.SrvMonitorService;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import org.zeromq.ZContext;

import javax.inject.Named;

import static com.namazustudios.socialengine.rt.Constants.IS_LOCAL_ENVIRONMENT_NAME;

public class JeroMQMultiplexerModule extends AbstractModule {
    private boolean isLocalInstance;

    @Override
    protected void configure() {
        bind(ZContext.class).asEagerSingleton();
        bind(ConnectionService.class).to(JeroMQMultiplexedConnectionService.class).asEagerSingleton();
        bind(InstanceMetadataContext.class).to(JeroMQInstanceMetadataContext.class).asEagerSingleton();
        bind(SrvMonitorService.class).to(SpotifySrvMonitorService.class).asEagerSingleton();

        if (isLocalInstance) {
            bind(InstanceDiscoveryService.class)
                    .to(StaticInstanceDiscoveryService.class)
                    .asEagerSingleton();
        }
        else {
            bind(InstanceDiscoveryService.class)
                    .to(SrvInstanceDiscoveryService.class)
                    .asEagerSingleton();
        }
    }

    public boolean getLocalInstance() {
        return isLocalInstance;
    }

    @Inject
    @Named(IS_LOCAL_ENVIRONMENT_NAME)
    public void setLocalInstance(boolean localInstance) {
        isLocalInstance = localInstance;
    }
}

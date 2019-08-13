package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.SimpleInstanceMetadataContext;
import com.namazustudios.socialengine.rt.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.remote.SpotifySrvInstanceDiscoveryService;
import com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService;
import org.zeromq.ZContext;

import javax.inject.Named;

import static com.namazustudios.socialengine.rt.Constants.IS_LOCAL_ENVIRONMENT_NAME;

public class JeroMQMultiplexerModule extends AbstractModule {
    private boolean isLocalInstance;

    @Override
    protected void configure() {

        bind(ZContext.class).asEagerSingleton();
        bind(InstanceMetadataContext.class).to(SimpleInstanceMetadataContext.class).asEagerSingleton();

        if (isLocalInstance) {
            bind(InstanceDiscoveryService.class)
                    .to(StaticInstanceDiscoveryService.class)
                    .asEagerSingleton();
        }
        else {
            bind(InstanceDiscoveryService.class)
                    .to(SpotifySrvInstanceDiscoveryService.class)
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

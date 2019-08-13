package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.remote.InstanceDiscoveryServiceFactory;

public class InstanceDiscoveryServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(InstanceDiscoveryService.class)
            .to(InstanceDiscoveryServiceFactory.getInstance().getInstanceDiscoveryServiceType())
            .asEagerSingleton();
    }

}

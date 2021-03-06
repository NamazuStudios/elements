package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.remote.SpotifySrvInstanceDiscoveryService;

public class SpotifySrvInstanceDiscoveryServiceModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(InstanceDiscoveryService.class);

        bind(InstanceDiscoveryService.class)
            .to(SpotifySrvInstanceDiscoveryService.class)
            .asEagerSingleton();

    }

}

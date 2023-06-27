package dev.getelements.elements.rt.remote.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.remote.InstanceDiscoveryService;
import dev.getelements.elements.rt.remote.JndiSrvInstanceDiscoveryService;

public class JndiSrvInstanceDiscoveryServiceModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(InstanceDiscoveryService.class);

        bind(InstanceDiscoveryService.class)
            .to(JndiSrvInstanceDiscoveryService.class)
            .asEagerSingleton();

    }

}

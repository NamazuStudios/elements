package dev.getelements.elements.rt.remote.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.remote.RemoteInvokerRegistry;
import dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry;

public class SimpleRemoteInvokerRegistryModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RemoteInvokerRegistry.class).to(SimpleRemoteInvokerRegistry.class).asEagerSingleton();
    }

}

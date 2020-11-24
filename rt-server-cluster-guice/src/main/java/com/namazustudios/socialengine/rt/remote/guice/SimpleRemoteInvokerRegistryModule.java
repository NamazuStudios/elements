package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.remote.SimpleRemoteInvokerRegistry;

public class SimpleRemoteInvokerRegistryModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RemoteInvokerRegistry.class).to(SimpleRemoteInvokerRegistry.class).asEagerSingleton();
    }

}

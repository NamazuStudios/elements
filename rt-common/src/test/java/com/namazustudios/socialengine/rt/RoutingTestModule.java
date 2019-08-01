package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.remote.RoutingStrategy;

import java.util.UUID;

import static com.google.inject.name.Names.named;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;

public class RoutingTestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RemoteInvokerRegistry.class).toInstance(mock(RemoteInvokerRegistry.class));
        bind(UUID.class).annotatedWith(named(RoutingStrategy.DEFAULT_APPLICATION)).toInstance(randomUUID());
    }

}

package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;

import static com.namazustudios.socialengine.rt.id.ApplicationId.randomApplicationId;
import static org.mockito.Mockito.mock;

public class RoutingTestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RemoteInvokerRegistry.class).toInstance(mock(RemoteInvokerRegistry.class));
        bind(ApplicationId.class).toInstance(randomApplicationId());
    }

}

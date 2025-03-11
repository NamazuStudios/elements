package dev.getelements.elements.rt;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.rt.remote.RemoteInvokerRegistry;

import static dev.getelements.elements.sdk.cluster.id.ApplicationId.randomApplicationId;
import static org.mockito.Mockito.mock;

public class RoutingTestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RemoteInvokerRegistry.class).toInstance(mock(RemoteInvokerRegistry.class));
        bind(ApplicationId.class).toInstance(randomApplicationId());
    }

}

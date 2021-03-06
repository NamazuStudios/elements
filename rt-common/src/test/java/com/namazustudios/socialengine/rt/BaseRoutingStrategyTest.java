package com.namazustudios.socialengine.rt;

import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.remote.RoutingStrategy;
import org.testng.annotations.BeforeMethod;

import static org.mockito.Mockito.reset;

public class BaseRoutingStrategyTest {

    private ApplicationId applicationId;

    private RoutingStrategy routingStrategy;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    @BeforeMethod
    public void resetMocks() {
        reset(getRemoteInvokerRegistry());
    }

    public RoutingStrategy getRoutingStrategy() {
        return routingStrategy;
    }

    @Inject
    public void setRoutingStrategy(RoutingStrategy routingStrategy) {
        this.routingStrategy = routingStrategy;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    @Inject
    public void setApplicationId(ApplicationId applicationId) {
        this.applicationId = applicationId;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

}

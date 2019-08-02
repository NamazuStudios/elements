package com.namazustudios.socialengine.rt;

import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.remote.RoutingStrategy;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import javax.inject.Named;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.remote.RoutingStrategy.DEFAULT_APPLICATION;
import static org.mockito.Mockito.reset;

public class BaseRoutingStrategyTest {

    private UUID defaultApplicationUuid;

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

    public UUID getDefaultApplicationUuid() {
        return defaultApplicationUuid;
    }

    @Inject
    public void setDefaultApplicationUuid(@Named(DEFAULT_APPLICATION) UUID defaultApplicationUuid) {
        this.defaultApplicationUuid = defaultApplicationUuid;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

}

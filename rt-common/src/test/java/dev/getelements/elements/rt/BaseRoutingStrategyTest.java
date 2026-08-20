package dev.getelements.elements.rt;

import com.google.inject.Inject;
import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.rt.remote.RemoteInvokerRegistry;
import dev.getelements.elements.rt.remote.RoutingStrategy;
import org.testng.annotations.BeforeMethod;

import static org.mockito.Mockito.reset;

public class BaseRoutingStrategyTest {

    private DeploymentId deploymentId;

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

    public DeploymentId getDeploymentId() {
        return deploymentId;
    }

    @Inject
    public void setDeploymentId(DeploymentId deploymentId) {
        this.deploymentId = deploymentId;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

}

package dev.getelements.elements.rt;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.rt.remote.RemoteInvokerRegistry;

import static dev.getelements.elements.sdk.cluster.id.DeploymentId.randomDeploymentId;
import static org.mockito.Mockito.mock;

public class RoutingTestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RemoteInvokerRegistry.class).toInstance(mock(RemoteInvokerRegistry.class));
        bind(DeploymentId.class).toInstance(randomDeploymentId());
    }

}

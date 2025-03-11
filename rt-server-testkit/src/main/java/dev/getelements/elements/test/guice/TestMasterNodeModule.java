package dev.getelements.elements.test.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.InstanceMetadataContext;
import dev.getelements.elements.rt.guice.SimpleInstanceMetadataContextModule;
import dev.getelements.elements.rt.guice.SimpleLoadMonitorServiceModule;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.*;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQNodeModule;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.annotation.RemoteScope.*;
import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_RT_PROTOCOL;
import static dev.getelements.elements.sdk.cluster.id.NodeId.forMasterNode;
import static dev.getelements.elements.rt.remote.Node.MASTER_NODE_NAME;

public class TestMasterNodeModule extends PrivateModule {

    private final NodeId nodeId;

    public TestMasterNodeModule(final InstanceId instanceId) {
        this.nodeId = forMasterNode(instanceId);
    }

    @Override
    protected void configure() {

        expose(InstanceMetadataContext.class);
        expose(Key.get(Node.class, named(MASTER_NODE_NAME)));

        bind(NodeId.class).toInstance(nodeId);

        install(new SimpleLoadMonitorServiceModule());
        install(new SimpleInstanceMetadataContextModule());

        install(new JeroMQNodeModule()
            .withAnnotation(named(MASTER_NODE_NAME))
            .withNodeName("integration-test-master-node")
            .withMinimumConnections(5)
            .withMaximumConnections(250));

        bind(NodeLifecycle.class)
            .to(MasterNodeLifecycle.class)
            .asEagerSingleton();

        bind(LocalInvocationDispatcher.class)
            .to(MasterNodeLocalInvocationDispatcher.class)
            .asEagerSingleton();


        bind(String.class)
                .annotatedWith(named(REMOTE_SCOPE))
                .toInstance(MASTER_SCOPE);

        bind(String.class)
                .annotatedWith(named(REMOTE_PROTOCOL))
                .toInstance(ELEMENTS_RT_PROTOCOL);

    }
}

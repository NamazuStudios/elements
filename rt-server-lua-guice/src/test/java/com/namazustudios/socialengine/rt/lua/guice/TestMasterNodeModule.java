package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.guice.SimpleInstanceMetadataContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleLoadMonitorServiceModule;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.id.NodeId.forMasterNode;
import static com.namazustudios.socialengine.rt.remote.Node.MASTER_NODE_NAME;

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

    }
}

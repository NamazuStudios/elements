package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.guice.SimpleInstanceMetadataContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleLoadMonitorServiceModule;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.remote.Node.MASTER_NODE_NAME;
import static com.namazustudios.socialengine.rt.remote.guice.NodeIdModule.forMasterNode;

public class MasterNodeModule extends PrivateModule {

    @Override
    protected void configure() {

        install(new SimpleLoadMonitorServiceModule());
        install(new SimpleInstanceMetadataContextModule());
        install(forMasterNode(getProvider(InstanceId.class)));

        install(new JeroMQNodeModule()
            .withNodeName("MasterNode")
        );

        bind(Node.class)
            .annotatedWith(named(MASTER_NODE_NAME))
            .to(Node.class);

        bind(NodeLifecycle.class)
            .to(MasterNodeLifecycle.class)
            .asEagerSingleton();

        bind(LocalInvocationDispatcher.class)
            .to(MasterNodeLocalInvocationDispatcher.class)
            .asEagerSingleton();

        expose(Key.get(Node.class, named(MASTER_NODE_NAME)));

    }

}

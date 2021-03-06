package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.guice.SimpleInstanceMetadataContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleLoadMonitorServiceModule;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.remote.LocalInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.MasterNodeLifecycle;
import com.namazustudios.socialengine.rt.remote.MasterNodeLocalInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.NodeLifecycle;
import com.namazustudios.socialengine.rt.remote.guice.NodeIdModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.remote.Node.MASTER_NODE_NAME;

public class MasterNodeModule extends PrivateModule {

    @Override
    protected void configure() {

        install(new SimpleLoadMonitorServiceModule());
        install(new SimpleInstanceMetadataContextModule());
        install(NodeIdModule.forMasterNode(getProvider(InstanceId.class)));

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

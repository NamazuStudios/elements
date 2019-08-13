package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.MasterNodeInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.NodeLifecycle;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ContextNodeLifecycle;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.remote.Node.MASTER_NODE_NAME;

public class MasterNodeModule extends PrivateModule {

    private static final Logger logger = LoggerFactory.getLogger(MasterNodeModule.class);

    @Override
    protected void configure() {

        install(new JeroMQNodeModule());

        bind(Node.class)
            .annotatedWith(named(MASTER_NODE_NAME))
            .to(Node.class);

        bind(NodeLifecycle.class)
            .to(ContextNodeLifecycle.class)
            .asEagerSingleton();

        bind(InvocationDispatcher.class)
            .to(MasterNodeInvocationDispatcher.class)
            .asEagerSingleton();

        expose(Key.get(Node.class, named(MASTER_NODE_NAME)));

    }

}

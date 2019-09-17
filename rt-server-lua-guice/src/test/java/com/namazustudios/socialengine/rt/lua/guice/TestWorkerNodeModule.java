package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.ContextNodeLifecycle;
import com.namazustudios.socialengine.rt.remote.MasterNodeLifecycle;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.NodeLifecycle;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;

import static com.namazustudios.socialengine.rt.lua.guice.JeroMQEmbeddedTestService.MAXIMUM_CONNECTIONS;
import static com.namazustudios.socialengine.rt.lua.guice.JeroMQEmbeddedTestService.MINIMUM_CONNECTIONS;

public class TestWorkerNodeModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(Node.class);

        bind(NodeLifecycle.class)
            .to(ContextNodeLifecycle.class)
            .asEagerSingleton();

        install(new JeroMQNodeModule()
            .withNodeName("integration-test-node")
            .withMinimumConnections(MINIMUM_CONNECTIONS)
            .withMaximumConnections(MAXIMUM_CONNECTIONS));

    }

}

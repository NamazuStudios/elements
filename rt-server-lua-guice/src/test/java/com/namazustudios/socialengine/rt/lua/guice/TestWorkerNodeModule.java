package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;

import java.util.List;

import static com.namazustudios.socialengine.rt.id.NodeId.forInstanceAndApplication;
import static com.namazustudios.socialengine.rt.lua.guice.JeroMQEmbeddedTestService.MAXIMUM_CONNECTIONS;
import static com.namazustudios.socialengine.rt.lua.guice.JeroMQEmbeddedTestService.MINIMUM_CONNECTIONS;

public class TestWorkerNodeModule extends PrivateModule {

    private final NodeId nodeId;

    private final List<Module> workerModules;

    public TestWorkerNodeModule(final InstanceId instanceId,
                                final ApplicationId applicationId,
                                final List<Module> workerModules) {
        this.nodeId = forInstanceAndApplication(instanceId, applicationId);
        this.workerModules = workerModules;
    }

    @Override
    protected void configure() {

        expose(Node.class);
        expose(RemoteInvocationDispatcher.class);

        workerModules.forEach(this::install);

        install(new GuiceIoCResolverModule());

        install(new JeroMQNodeModule()
            .withNodeName("integration-test-node")
            .withMinimumConnections(MINIMUM_CONNECTIONS)
            .withMaximumConnections(MAXIMUM_CONNECTIONS)
        );

        bind(NodeId.class).toInstance(nodeId);

        bind(LocalInvocationDispatcher.class)
            .to(ContextLocalInvocationDispatcher.class)
            .asEagerSingleton();

        bind(RemoteInvocationDispatcher.class)
            .to(SimpleRemoteInvocationDispatcher.class)
            .asEagerSingleton();

    }

}

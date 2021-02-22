package com.namazustudios.socialengine.test.guice;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceServiceModule;
import com.namazustudios.socialengine.rt.xodus.XodusSchedulerContextModule;

import java.util.List;

import static com.namazustudios.socialengine.rt.id.NodeId.forInstanceAndApplication;
import static com.namazustudios.socialengine.test.JeroMQEmbeddedWorkerInstanceContainer.MAXIMUM_CONNECTIONS;
import static com.namazustudios.socialengine.test.JeroMQEmbeddedWorkerInstanceContainer.MINIMUM_CONNECTIONS;

public class TestWorkerNodeModule extends PrivateModule {

    private final NodeId nodeId;

    private final List<Module> workerModules;

    public TestWorkerNodeModule(final NodeId nodeId, final List<Module> workerModules) {
        this.nodeId = nodeId;
        this.workerModules = workerModules;
    }

    @Override
    protected void configure() {

        expose(Node.class);
        expose(NodeId.class);
        expose(IocResolver.class);
        expose(RemoteInvocationDispatcher.class);

        workerModules.forEach(this::install);

        install(new ClusterContextModule());

        install(new TestNodeServicesModule());
        install(new GuiceIoCResolverModule());
        install(new TransactionalResourceServiceModule());

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

        install(new SimpleContextModule()
            .withDefaultContexts()
            .withSchedulerContextModules(new XodusSchedulerContextModule())
        );

    }

}

package dev.getelements.elements.test.guice;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.IocResolver;
import dev.getelements.elements.rt.guice.GuiceIoCResolverModule;
import dev.getelements.elements.rt.guice.SimpleContextModule;
import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.remote.*;
import dev.getelements.elements.rt.remote.guice.ClusterContextModule;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQNodeModule;
import dev.getelements.elements.rt.transact.TransactionalResourceServiceModule;
import dev.getelements.elements.rt.xodus.XodusSchedulerContextModule;

import java.util.Collection;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.annotation.RemoteScope.*;
import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_RT_PROTOCOL;
import static dev.getelements.elements.test.JeroMQEmbeddedWorkerInstanceContainer.MAXIMUM_CONNECTIONS;
import static dev.getelements.elements.test.JeroMQEmbeddedWorkerInstanceContainer.MINIMUM_CONNECTIONS;

public class TestApplicationNodeModule extends PrivateModule {

    private final NodeId nodeId;

    private final Collection<? extends Module> workerModules;

    public TestApplicationNodeModule(final NodeId nodeId, final Collection<? extends Module> workerModules) {
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
        bind(ApplicationId.class).toInstance(nodeId.getApplicationId());

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

        bind(String.class)
                .annotatedWith(named(REMOTE_SCOPE))
                .toInstance(WORKER_SCOPE);

        bind(String.class)
                .annotatedWith(named(REMOTE_PROTOCOL))
                .toInstance(ELEMENTS_RT_PROTOCOL);

    }

}

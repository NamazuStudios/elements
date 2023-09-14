package dev.getelements.elements.appnode.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.rt.SimpleAttributes;
import dev.getelements.elements.rt.guice.FileAssetLoaderModule;
import dev.getelements.elements.rt.guice.GuiceIoCResolverModule;
import dev.getelements.elements.rt.guice.SimpleContextModule;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.lua.guice.LuaModule;
import dev.getelements.elements.rt.remote.*;
import dev.getelements.elements.rt.remote.guice.ClusterContextModule;
import dev.getelements.elements.rt.remote.guice.NodeIdModule;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQNodeModule;
import dev.getelements.elements.rt.transact.TransactionalResourceServiceModule;
import dev.getelements.elements.rt.xodus.XodusSchedulerContextModule;

import java.io.File;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.annotation.RemoteScope.*;
import static java.lang.String.format;

public class LuaApplicationModule extends PrivateModule {

    private final NodeId nodeId;

    private final Application application;

    private final File codeDirectory;

    public LuaApplicationModule(final NodeId nodeId,
                                final Application application,
                                final File codeDirectory) {
        this.nodeId = nodeId;
        this.application = application;
        this.codeDirectory = codeDirectory;
    }

    @Override
    protected void configure() {

        final var applicationDaoProvider = getProvider(ApplicationDao.class);

        final var attributes = new SimpleAttributes.Builder()
                .setAttributes(application.getAttributes())
                .build();

        final var applicationId = application.getId();

        bind(Application.class).toProvider(() -> applicationDaoProvider.get().getActiveApplication(applicationId));


        bind(LocalInvocationDispatcher.class)
            .to(ContextLocalInvocationDispatcher.class)
            .asEagerSingleton();

        bind(RemoteInvocationDispatcher.class)
            .to(SimpleRemoteInvocationDispatcher.class)
            .asEagerSingleton();

        install(new LuaModule().withAttributes(attributes));
        install(new GuiceIoCResolverModule());
        install(new ContextServicesModule());
        install(new TransactionalResourceServiceModule());
        install(new NodeIdModule(nodeId));
        install(new ClusterContextModule());
        install(new FileAssetLoaderModule(codeDirectory));

        install(new SimpleContextModule()
            .withDefaultContexts()
            .withSchedulerContextModules(new XodusSchedulerContextModule())
        );

        install(new JeroMQNodeModule()
            .withNodeName(format("app.%s.%s", application.getName(), application.getId()))
        );

        bind(String.class)
                .annotatedWith(named(REMOTE_SCOPE))
                .toInstance(WORKER_SCOPE);

        bind(String.class)
                .annotatedWith(named(REMOTE_PROTOCOL))
                .toInstance(ELEMENTS_RT_PROTOCOL);

        expose(Node.class);
        expose(NodeLifecycle.class);
        expose(LocalInvocationDispatcher.class);

    }

}

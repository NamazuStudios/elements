package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.guice.FileAssetLoaderModule;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextModule;
import com.namazustudios.socialengine.rt.remote.guice.NodeIdModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceServiceModule;
import com.namazustudios.socialengine.rt.xodus.XodusSchedulerContextModule;

import java.io.File;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.*;
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

        final var applicationId = application.getId();
        bind(Application.class).toProvider(() -> applicationDaoProvider.get().getActiveApplication(applicationId));

        bind(LocalInvocationDispatcher.class)
            .to(ContextLocalInvocationDispatcher.class)
            .asEagerSingleton();

        bind(RemoteInvocationDispatcher.class)
            .to(SimpleRemoteInvocationDispatcher.class)
            .asEagerSingleton();

        install(new LuaModule());
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

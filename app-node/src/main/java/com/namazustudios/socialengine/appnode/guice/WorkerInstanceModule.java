package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.rt.exception.ApplicationCodeNotFoundException;
import com.namazustudios.socialengine.rt.git.GitLoader;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.SimpleWorkerInstanceModule;
import com.namazustudios.socialengine.rt.remote.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;

public class WorkerInstanceModule extends PrivateModule {

    private static final Logger logger = LoggerFactory.getLogger(WorkerInstanceModule.class);

    @Override
    protected void configure() {

        install(new SimpleWorkerInstanceModule());

        bind(new TypeLiteral<Set<Node>>(){})
            .toProvider(nodeProvider())
            .asEagerSingleton();

        expose(Worker.class);
        expose(Instance.class);

    }

    private Provider<Set<Node>> nodeProvider() {

        final var instanceIdProvider = getProvider(InstanceId.class);
        final var applicationDaoProvider = getProvider(ApplicationDao.class);
        final var injectorProvider = getProvider(Injector.class);
        final var gitLoaderProvider = getProvider(GitLoader.class);

        return () -> {

            final var injector = injectorProvider.get();
            final var gitLoader = gitLoaderProvider.get();
            final var applicationDao = applicationDaoProvider.get();

            final var applications = applicationDao.getActiveApplications().getObjects();

            final Set<Node> nodeSet = applications.stream().map(application -> {

                    final File codeDirectory;
                    final var appId = ApplicationId.forUniqueName(application.getId());

                    try {
                        codeDirectory = gitLoader.getCodeDirectory(appId);
                    } catch (ApplicationCodeNotFoundException nfe) {
                        logger.info("Application code not found.  Skipping application {}", application.getName());
                        return null;
                    }

                    final var nodeId = NodeId.forInstanceAndApplication(instanceIdProvider.get(), appId);
                    final var appModule = new LuaApplicationModule(nodeId, application, codeDirectory);

                    final var nodeInjector = injector.createChildInjector(appModule);
                    return nodeInjector.getInstance(Node.class);

                }).filter(Objects::nonNull).collect(toCollection(LinkedHashSet::new));

            return unmodifiableSet(nodeSet);

        };

    }

}

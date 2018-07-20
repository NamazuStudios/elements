package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.rt.GitLoader;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer;
import com.namazustudios.socialengine.rt.ConnectionDemultiplexer;
import com.namazustudios.socialengine.rt.MultiNodeContainer;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.appnode.Constants.STORAGE_BASE_DIRECTORY;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;

public class MultiNodeContainerModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(MultiNodeContainerModule.class);

    @Override
    protected void configure() {

        bind(ZContext.class).asEagerSingleton();
        bind(MultiNodeContainer.class).asEagerSingleton();

        bind(ConnectionDemultiplexer.class)
            .to(JeroMQConnectionDemultiplexer.class)
            .asEagerSingleton();

        bind(new TypeLiteral<Set<Node>>(){})
            .toProvider(nodeProvider())
            .asEagerSingleton();

    }

    private Provider<Set<Node>> nodeProvider() {

        final Provider<ApplicationDao> applicationDaoProvider = getProvider(ApplicationDao.class);
        final Provider<Injector> injectorProvider = getProvider(Injector.class);
        final Provider<GitLoader> gitLoaderProvider = getProvider(GitLoader.class);
        final Provider<ConnectionDemultiplexer> connectionDemultiplexerProvider = getProvider(ConnectionDemultiplexer.class);
        final Provider<File> resourcesStorageBaseDirectoryProvider = getProvider(Key.get(File.class, named(STORAGE_BASE_DIRECTORY)));

        return () -> {

            final ApplicationDao applicationDao = applicationDaoProvider.get();
            final Injector injector = injectorProvider.get();
            final GitLoader gitLoader = gitLoaderProvider.get();
            final ConnectionDemultiplexer connectionDemultiplexer = connectionDemultiplexerProvider.get();

            final Set<Node> nodeSet = applicationDao.getActiveApplications().getObjects().stream()
                .map(application -> {

                    final File codeDirectory;

                    try {
                        codeDirectory = gitLoader.getCodeDirectory(application);
                    } catch (NotFoundException nfe) {
                        logger.info("Application code not found.  Skipping application {}", application.getName());
                        return null;
                    }

                    final UUID uuid = connectionDemultiplexer.getDestinationUUIDForNodeId(application.getId());
                    final String bindAddress = connectionDemultiplexer.getBindAddress(uuid);

                    final JeroMQNodeModule nodeModule = new JeroMQNodeModule()
                        .withBindAddress(bindAddress)
                        .withNodeId(application.getId())
                        .withNodeName(application.getName());

                    final File storageDiretory = getStorageDirectoryForApplication(resourcesStorageBaseDirectoryProvider, application);
                    final ApplicationModule applicationModule = new ApplicationModule(application, codeDirectory, storageDiretory);
                    final Injector nodeInjector = injector.createChildInjector(applicationModule, nodeModule);

                    return nodeInjector.getInstance(Node.class);

                }).filter(node -> node != null).collect(toCollection(LinkedHashSet::new));

            return unmodifiableSet(nodeSet);

        };

    }

    private File getStorageDirectoryForApplication(final Provider<File> resourcesStorageBaseDirectoryProvider,
                                                   final Application application) {
        return new File(resourcesStorageBaseDirectoryProvider.get(), application.getId());
    }

}

package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.appnode.provider.DestinationIDsProvider;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.rt.GitLoader;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer;
import com.namazustudios.socialengine.rt.ConnectionDemultiplexer;
import com.namazustudios.socialengine.rt.MultiNodeContainer;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.guice.SimpleServicesModule;
import com.namazustudios.socialengine.rt.jeromq.Routing;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static com.google.inject.name.Names.named;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toCollection;

public class MultiNodeContainerModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(MultiNodeContainerModule.class);

    @Override
    protected void configure() {

        install(new SimpleServicesModule());

        bind(ZContext.class).asEagerSingleton();
        bind(MultiNodeContainer.class).asEagerSingleton();

        bind(ConnectionDemultiplexer.class)
            .to(JeroMQConnectionDemultiplexer.class)
            .asEagerSingleton();

        bind(new TypeLiteral<Set<Node>>(){})
            .toProvider(nodeProvider())
            .asEagerSingleton();

        bind(new TypeLiteral<Set<String>>(){})
            .annotatedWith(named(JeroMQConnectionDemultiplexer.DESTINATION_IDS))
            .toProvider(DestinationIDsProvider.class);

    }

    private Provider<Set<Node>> nodeProvider() {

        final Provider<Routing> routingProvider = getProvider(Routing.class);
        final Provider<ApplicationDao> applicationDaoProvider = getProvider(ApplicationDao.class);
        final Provider<Injector> injectorProvider = getProvider(Injector.class);
        final Provider<GitLoader> gitLoaderProvider = getProvider(GitLoader.class);

        return () -> {

            final Routing routing = routingProvider.get();
            final ApplicationDao applicationDao = applicationDaoProvider.get();
            final Injector injector = injectorProvider.get();
            final GitLoader gitLoader = gitLoaderProvider.get();

            final Set<Node> nodeSet = applicationDao.getActiveApplications().getObjects().stream()
                .map(application -> {

                    final File codeDirectory;

                    try {
                        codeDirectory = gitLoader.getCodeDirectory(application);
                    } catch (NotFoundException nfe) {
                        logger.info("Application code not found.  Skipping application.");
                        return null;
                    }

                    final UUID uuid = routing.getDestinationId(application.getId());
                    final String bindAddress = routing.getDemultiplexedAddressForDestinationId(uuid);
                    final JeroMQNodeModule nodeModule = new JeroMQNodeModule().withBindAddress(bindAddress);

                    final ApplicationModule applicationModule = new ApplicationModule(codeDirectory);
                    final Injector nodeInjector = injector.createChildInjector(applicationModule, nodeModule);
                    return nodeInjector.getInstance(Node.class);

                }).filter(node -> node != null).collect(toCollection(LinkedHashSet::new));

            return unmodifiableSet(nodeSet);

        };

    }

}

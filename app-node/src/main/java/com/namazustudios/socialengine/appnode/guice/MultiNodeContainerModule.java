package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.rt.GitLoader;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.appnode.Constants.STORAGE_BASE_DIRECTORY;
import static com.namazustudios.socialengine.rt.Constants.IS_LOCAL_ENVIRONMENT_NAME;
import static com.namazustudios.socialengine.rt.Node.MASTER_NODE_NAME;
import static com.namazustudios.socialengine.rt.id.ApplicationId.forUniqueName;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;

public class MultiNodeContainerModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(MultiNodeContainerModule.class);


    @Override
    protected void configure() {

        final Provider<Boolean> isLocalInstanceProvider = getProvider(Key.get(Boolean.class, named(IS_LOCAL_ENVIRONMENT_NAME)));
        final boolean isLocalInstance = isLocalInstanceProvider.get();

        install(new ZContextModule());
        bind(SimpleInstance.class).asEagerSingleton();

        if (isLocalInstance) {
            bind(InstanceDiscoveryService.class)
                .to(StaticInstanceDiscoveryService.class)
                .asEagerSingleton();
        } else {
            bind(InstanceDiscoveryService.class)
                .to(SpotifySrvInstanceDiscoveryService.class)
                .asEagerSingleton();
        }

        bind(new TypeLiteral<Set<Node>>(){})
            .toProvider(nodeProvider())
            .asEagerSingleton();

        bind(Node.class)
            .annotatedWith(named(MASTER_NODE_NAME))
            .toProvider(masterNodeProvider());

    }

    private Provider<Node> masterNodeProvider() {
        final Provider<Injector> injectorProvider = getProvider(Injector.class);

        return () -> {
            final Injector injector = injectorProvider.get();
            final Injector masterNodeInjector = injector.createChildInjector(new MasterNodeModule());
            final Node masterNode = masterNodeInjector.getInstance(Node.class);
            return masterNode;
        };
    }

    private Provider<Set<Node>> nodeProvider() {

        final Provider<InstanceId> instanceIdProvider = getProvider(InstanceId.class);
        final Provider<ApplicationDao> applicationDaoProvider = getProvider(ApplicationDao.class);
        final Provider<Injector> injectorProvider = getProvider(Injector.class);
        final Provider<GitLoader> gitLoaderProvider = getProvider(GitLoader.class);
        final Provider<File> resourcesStorageBaseDirectoryProvider = getProvider(Key.get(File.class, named(STORAGE_BASE_DIRECTORY)));

        return () -> {

            final ApplicationDao applicationDao = applicationDaoProvider.get();
            final Injector injector = injectorProvider.get();
            final GitLoader gitLoader = gitLoaderProvider.get();

            final Set<Node> nodeSet = applicationDao.getActiveApplications().getObjects().stream()
                .map(application -> {

                    final File codeDirectory;

                    try {
                        codeDirectory = gitLoader.getCodeDirectory(application);
                    } catch (NotFoundException nfe) {
                        logger.info("Application code not found.  Skipping application {}", application.getName());
                        return null;
                    }

                    final InstanceId instanceId = instanceIdProvider.get();
                    final ApplicationId applicationId = forUniqueName(application.getId());
                    final NodeId nodeId = new NodeId(instanceId, applicationId);

                    final JeroMQNodeModule nodeModule = new JeroMQNodeModule()
                        .withNodeId(nodeId)
                        .withNodeName(application.getName());

                    final File storageDirectory = getStorageDirectoryForApplication(resourcesStorageBaseDirectoryProvider, application);
                    final ApplicationModule applicationModule = new ApplicationModule(application, codeDirectory, storageDirectory);
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

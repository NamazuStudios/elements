package dev.getelements.elements.appnode.guice;

import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.exception.ApplicationCodeNotFoundException;
import dev.getelements.elements.rt.git.GitApplicationAssetLoader;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.SimpleWorkerInstanceModule;
import dev.getelements.elements.rt.remote.Worker;
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

        bind(Node.Factory.class)
            .toInstance(Node.Factory.unsupported());

        expose(Worker.class);
        expose(Instance.class);

    }

    private Provider<Set<Node>> nodeProvider() {

        final var instanceIdProvider = getProvider(InstanceId.class);
        final var applicationDaoProvider = getProvider(ApplicationDao.class);
        final var injectorProvider = getProvider(Injector.class);
        final var gitLoaderProvider = getProvider(GitApplicationAssetLoader.class);

        return () -> {

            final var injector = injectorProvider.get();
            final var gitLoader = gitLoaderProvider.get();
            final var applicationDao = applicationDaoProvider.get();

            final var applications = applicationDao.getActiveApplications().getObjects();

            final Set<Node> nodeSet = applications.stream().map(application -> {

                    final File codeDirectory;
                    final var appId = ApplicationId.forUniqueName(application.getId());

                    try {
//                        codeDirectory = gitLoader.getCodeDirectory(appId);
                    } catch (ApplicationCodeNotFoundException nfe) {
                        logger.info("Application code not found.  Skipping application {}", application.getName());
                        return null;
                    }

                    final var nodeId = NodeId.forInstanceAndApplication(instanceIdProvider.get(), appId);

                    // TODO: Replace with Loader
                    final var nodeInjector = injector.createChildInjector();
                    return nodeInjector.getInstance(Node.class);

                }).filter(Objects::nonNull).collect(toCollection(LinkedHashSet::new));

            return unmodifiableSet(nodeSet);

        };

    }

}

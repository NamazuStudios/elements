package com.namazustudios.socialengine.test;

import com.google.inject.Module;
import com.google.inject.*;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleExecutorsModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.jeromq.JeroMQAsyncConnectionService;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextModule;
import com.namazustudios.socialengine.rt.remote.guice.StaticInstanceDiscoveryServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQControlClientModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQInstanceConnectionServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQRemoteInvokerModule;
import com.namazustudios.socialengine.rt.transact.SimpleTransactionalResourceServicePersistenceModule;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceServiceModule;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionalPersistenceContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusSchedulerContextModule;
import com.namazustudios.socialengine.test.guice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Embeds a test kit which supplies an instance of {@link Context} and {@link Node}.
 */
public class JeroMQEmbeddedTestService implements EmbeddedTestService {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQEmbeddedTestService.class);

    public static final int MINIMUM_CONNECTIONS = 5;

    public static final int MAXIMUM_CONNECTIONS = 250;

    private Instance client;

    private Injector clientInjector;

    private Worker worker;

    private Instance workerInstance;

    private Injector workerInjector;

    private final ZContext zContext = new ZContext();

    private final List<Module> workerModules = new ArrayList<>();

    private final List<Module> clientModules = new ArrayList<>();

    public JeroMQEmbeddedTestService() {}

    public JeroMQEmbeddedTestService withWorkerModule(final Module module) {
        workerModules.add(module);
        return this;
    }

    public JeroMQEmbeddedTestService withClientModule(final Module module) {
        clientModules.add(module);
        return this;
    }

    public JeroMQEmbeddedTestService withDefaultHttpClient() {
        return withWorkerModule(binder -> binder
            .bind(Client.class)
            .toProvider(ClientBuilder::newClient)
            .asEagerSingleton());
    }

    @Override
    public EmbeddedTestService start() {

        if (client != null || workerInstance != null) throw new IllegalStateException("Already started.");

        final var prefix = JeroMQEmbeddedTestService.class.getSimpleName();
        final var clientInstanceId = InstanceId.forUniqueName(format("%s.client", prefix));
        final var workerInstanceId = InstanceId.forUniqueName(format("%s.worker", prefix));
        final var applicationId = ApplicationId.forUniqueName(format("%s.application", prefix));

        final var clientBindAddress = String.format("inproc://integration-test-client/%s", clientInstanceId.asString());
        final var workerBindAddress = String.format("inproc://integration-test-worker/%s", workerInstanceId.asString());

        final var commonModule = new AbstractModule() {
            @Override
            protected void configure() {

                final Provider<JeroMQAsyncConnectionService> provider = getProvider(JeroMQAsyncConnectionService.class);
                bind(ApplicationId.class).toInstance(applicationId);

                bind(ZContext.class).toProvider(() -> ZContext.shadow(zContext));

                bind(JeroMQAsyncConnectionService.class).asEagerSingleton();

                bind(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){})
                    .toProvider(() -> new SharedAsyncConnectionService<>(provider.get()))
                    .asEagerSingleton();

                bind(new TypeLiteral<AsyncConnectionService<?,?>>(){})
                    .to(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){});

                bind(RemoteInvokerRegistry.class)
                    .to(SimpleRemoteInvokerRegistry.class)
                    .asEagerSingleton();

                install(new StaticInstanceDiscoveryServiceModule()
                    .withInstanceAddresses(workerBindAddress));

                install(new JeroMQRemoteInvokerModule()
                    .withMinimumConnections(MINIMUM_CONNECTIONS)
                    .withMaximumConnections(MAXIMUM_CONNECTIONS));

                install(new JeroMQControlClientModule());

            }
        };

        final var workerModule = new AbstractModule() {
            @Override
            protected void configure() {

                bind(InstanceId.class).toInstance(workerInstanceId);
                bind(ApplicationId.class).toInstance(applicationId);
                bind(AssetLoader.class).toProvider(() -> new ClasspathAssetLoader(ClassLoader.getSystemClassLoader()));

                final var allWorkerModules = new ArrayList<>(workerModules);

                allWorkerModules.add(new TestServicesModule());
                allWorkerModules.add(new TransactionalResourceServiceModule());
                allWorkerModules.add(new SimpleContextModule()
                    .withDefaultContexts()
                    .withSchedulerContextModules(new XodusSchedulerContextModule())
                );

                install(commonModule);
                install(new ClusterContextModule());
                install(new FSTPayloadReaderWriterModule());
                install(new TestWorkerInstanceModule());
                install(new TestMasterNodeModule(workerInstanceId));
                install(new TestWorkerNodeModule(workerInstanceId, applicationId, allWorkerModules));
                install(new JeroMQInstanceConnectionServiceModule()
                    .withBindAddress(workerBindAddress)
                    .withDefaultRefreshInterval());
                install(new SimpleExecutorsModule().withDefaultSchedulerThreads());
                install(new SimpleTransactionalResourceServicePersistenceModule());
                install(new UnixFSTransactionalPersistenceContextModule().withTestingDefaults());

            }
        };

        final var clientModule = new AbstractModule() {
            @Override
            protected void configure() {

                bind(InstanceId.class).toInstance(clientInstanceId);
                bind(ApplicationId.class).toInstance(applicationId);
                bind(NodeId.class).toInstance(NodeId.forInstanceAndApplication(clientInstanceId, applicationId));

                bind(RemoteInvocationDispatcher.class)
                    .to(SimpleRemoteInvocationDispatcher.class)
                    .asEagerSingleton();

                install(commonModule);
                clientModules.forEach(this::install);

                install(new GuiceIoCResolverModule());
                install(new ClusterContextModule());
                install(new FSTPayloadReaderWriterModule());
                install(new TestClientInstanceModule());
                install(new JeroMQInstanceConnectionServiceModule()
                    .withBindAddress(clientBindAddress)
                    .withDefaultRefreshInterval()
                );

            }
        };

        workerInjector = Guice.createInjector(workerModule);
        worker = workerInjector.getInstance(Worker.class);
        workerInstance = workerInjector.getInstance(Instance.class);

        clientInjector = Guice.createInjector(clientModule);
        client = clientInjector.getInstance(Instance.class);

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            getWorkerInstance().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception starting test worker instance.", ex);
        }

        try {
            getClient().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception starting test client instance.", ex);
        }

        getWorkerInstance().refreshConnections();
        getClient().refreshConnections();

        if (!exceptionList.isEmpty()) throw new MultiException(exceptionList);

        return this;

    }

    @Override
    public Instance getClient() {
        return client;
    }

    @Override
    public Worker getWorker() {
        return worker;
    }

    @Override
    public Instance getWorkerInstance() {
        return workerInstance;
    }

    @Override
    public void close() {

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            getClient().close();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception stopping test client instance.", ex);
        }

        try {
            getWorkerInstance().close();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception stopping test worker instance.", ex);
        }

        if (!exceptionList.isEmpty()) throw new MultiException(exceptionList);

    }

    @Override
    public IocResolver getClientIocResolver() {
        return clientInjector.getInstance(IocResolver.class);
    }

    @Override
    public IocResolver getWorkerIocResolver() {
        return workerInjector.getInstance(IocResolver.class);
    }

}

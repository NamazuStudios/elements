package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.Module;
import com.google.inject.*;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.guice.SimpleExecutorsModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.jeromq.JeroMQAsyncConnectionService;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.remote.SimpleRemoteInvokerRegistry;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextModule;
import com.namazustudios.socialengine.rt.remote.guice.StaticInstanceDiscoveryServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQInstanceConnectionServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQRemoteInvokerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.ArrayList;
import java.util.List;

import static com.google.inject.Key.get;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.REMOTE;
import static com.namazustudios.socialengine.rt.id.ApplicationId.randomApplicationId;
import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static com.namazustudios.socialengine.rt.id.NodeId.forInstanceAndApplication;
import static java.lang.String.format;
import static org.zeromq.ZContext.shadow;

/**
 * Embeds a test kit which supplies an instance of {@link Context} and {@link Node}.
 */
public class JeroMQEmbeddedTestService implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQEmbeddedTestService.class);

    public static final int MINIMUM_CONNECTIONS = 5;

    public static final int MAXIMUM_CONNECTIONS = 250;

    private Context context;

    private Instance worker;

    private Instance client;

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
        return withWorkerModule(binder -> binder.bind(Client.class).toProvider(ClientBuilder::newClient).asEagerSingleton());
    }

    public JeroMQEmbeddedTestService start() {

        final InstanceId clientInstanceId = randomInstanceId();
        final InstanceId workerInstanceId = randomInstanceId();
        final ApplicationId applicationId = randomApplicationId();

        final String clientBindAddress = format("inproc://integration-test-client/%s", clientInstanceId.asString());
        final String workerBindAddress = format("inproc://integration-test-worker/%s", workerInstanceId.asString());

        final Module commonModule = new AbstractModule() {
            @Override
            protected void configure() {

                final Provider<JeroMQAsyncConnectionService> provider = getProvider(JeroMQAsyncConnectionService.class);
                bind(ApplicationId.class).toInstance(applicationId);

                bind(ZContext.class).toProvider(() -> shadow(zContext));

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

            }
        };

        final Module workerModule = new AbstractModule() {
            @Override
            protected void configure() {

                bind(InstanceId.class).toInstance(workerInstanceId);
                bind(ApplicationId.class).toInstance(applicationId);
                bind(AssetLoader.class).toProvider(() -> new ClasspathAssetLoader(ClassLoader.getSystemClassLoader()));

                install(commonModule);
                install(new ClusterContextModule());
                install(new FSTPayloadReaderWriterModule());
                install(new TestWorkerInstanceModule());
                install(new TestMasterNodeModule(workerInstanceId));
                install(new TestWorkerNodeModule(workerInstanceId, applicationId, workerModules));
                install(new JeroMQInstanceConnectionServiceModule().withBindAddress(workerBindAddress));
                install(new SimpleExecutorsModule().withDefaultSchedulerThreads());

            }
        };

        final Module clientModule = new AbstractModule() {
            @Override
            protected void configure() {

                bind(InstanceId.class).toInstance(clientInstanceId);
                bind(ApplicationId.class).toInstance(applicationId);
                bind(NodeId.class).toInstance(forInstanceAndApplication(clientInstanceId, applicationId));

                install(commonModule);
                clientModules.forEach(this::install);

                install(new ClusterContextModule());
                install(new FSTPayloadReaderWriterModule());
                install(new TestClientInstanceModule());
                install(new JeroMQInstanceConnectionServiceModule()
                    .withBindAddress(clientBindAddress));

            }
        };

        final Injector workerInjector = Guice.createInjector(workerModule);
        worker = workerInjector.getInstance(Instance.class);

        final Injector clientInjector = Guice.createInjector(clientModule);
        client = clientInjector.getInstance(Instance.class);
        context = clientInjector.getInstance(get(Context.class, named(REMOTE)));

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            getWorker().start();
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

        if (!exceptionList.isEmpty()) throw new MultiException(exceptionList);

        return this;

    }

    public Context getContext() {
        return context;
    }

    public Instance getClient() {
        return client;
    }

    public Instance getWorker() {
        return worker;
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
            getWorker().close();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Exception stopping test worker instance.", ex);
        }

        if (!exceptionList.isEmpty()) throw new MultiException(exceptionList);

    }

}

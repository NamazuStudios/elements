package dev.getelements.elements.rt.remote.jeromq.guice;

import com.google.inject.*;
import com.google.inject.name.Named;
import dev.getelements.elements.remote.jeromq.JeroMQNode;
import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.guice.SimpleExecutorsModule;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.jeromq.JeroMQAsyncConnectionService;
import dev.getelements.elements.rt.kryo.guice.KryoPayloadReaderWriterModule;
import dev.getelements.elements.rt.remote.*;
import dev.getelements.elements.rt.remote.guice.StaticInstanceDiscoveryServiceModule;
import dev.getelements.elements.rt.remote.jeromq.JeroMQCurveSecurity;
import dev.getelements.elements.rt.remote.jeromq.JeroMQSecurity;
import dev.getelements.elements.sdk.guice.GuiceServiceLocatorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.annotation.RemoteScope.*;
import static dev.getelements.elements.sdk.cluster.id.ApplicationId.randomApplicationId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.sdk.cluster.id.NodeId.forInstanceAndApplication;
import static dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry.*;
import static java.lang.String.format;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class JeroMQEndToEndIntegrationTest {

    private static final int TEST_NODE_COUNT = 2;

    private static final Logger logger = LoggerFactory.getLogger(JeroMQEndToEndIntegrationTest.class);

    private static final JeroMQSecurity TEST_SECURITY = new JeroMQCurveSecurity();

    private Instance client;

    private List<Instance> workers;

    private TestServiceInterface testServiceInterface;

    @BeforeClass
    public void setup() {

        final Injector sharedInjector = Guice.createInjector(
                new ZContextModule()
                        .withMaxSockets(8096)
                        .withDefaultIoThreads()
                        .withDefaultIpv6(),
                new AbstractModule() {
                    @Override
                    protected void configure() {

                        final var provider = getProvider(JeroMQAsyncConnectionService.class);

                        bind(JeroMQAsyncConnectionService.class).asEagerSingleton();
                        bind(String.class)
                            .annotatedWith(named(JeroMQAsyncConnectionService.ASYNC_CONNECTION_IO_THREADS))
                            .toInstance("1");

                        bind(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){})
                            .toProvider(() -> new SharedAsyncConnectionService<>(provider.get()))
                            .asEagerSingleton();

                        bind(new TypeLiteral<AsyncConnectionService<?,?>>(){})
                            .to(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){});

                        bind(JeroMQSecurity.class).toInstance(TEST_SECURITY);

                    }
                });

        final var zContext = sharedInjector.getInstance(ZContext.class);
        final var asyncConnectionService = sharedInjector
            .getInstance(Key.get(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){}));

        final ApplicationId applicationId = randomApplicationId();
        final List<InstanceId> instanceIdList = IntStream.range(0, TEST_NODE_COUNT)
            .mapToObj(i -> randomInstanceId())
            .collect(toList());

        final var clientInjector = createInjector(new ClientModule(zContext, asyncConnectionService, applicationId, instanceIdList));
        setTestServiceInterface(clientInjector.getInstance(TestServiceInterface.class));
        client = clientInjector.getInstance(Instance.class);

        final var workerInjector = createInjector(new WorkerInstanceModule(zContext, asyncConnectionService, applicationId, instanceIdList));
        workers = instanceIdList.stream()
            .map(instanceId -> workerInjector.getInstance(Key.get(Instance.class, named(instanceId.asString()))))
            .collect(toList());

    }

    @AfterClass
    public void shutdown() {
        client.close();
        workers.forEach(Instance::close);
        workers.clear();
    }

    @Test(enabled = false)
    public void startWorkers() {
        workers.forEach(Instance::start);
    }

    @Test(enabled = false, dependsOnMethods = "startWorkers")
    void refreshPeers() {
        workers.forEach(Instance::refreshConnections);
    }

    @Test(enabled = false, dependsOnMethods = "refreshPeers")
    public void startClient() {
        client.start();
        client.refreshConnections();
    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testRemoteInvokeSync() {
        getTestServiceInterface().testSyncVoid("Hello");
    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1, expectedExceptions = IllegalArgumentException.class)
    public void testRemoteInvokeSyncException() {
        getTestServiceInterface().testSyncVoid("testSyncVoid");
    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testRemoteInvokeSyncReturn() {
        final double result = getTestServiceInterface().testSyncReturn("Hello");
        assertEquals(result, 40.42);
    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, expectedExceptions = IllegalArgumentException.class)
    public void testRemoteInvokeSyncReturnException() {
        getTestServiceInterface().testSyncReturn("testSyncReturn");
        fail("Did not expect return.");
    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testAsyncReturnVoid() throws Exception {
        final BlockingQueue<Callable<?>> bq = new LinkedBlockingDeque<>();
        getTestServiceInterface().testAsyncReturnVoid(
            "Hello",
            result -> bq.add(() -> {
                assertEquals(result, "World!");
                return null;
            }),
            throwable -> bq.add(() -> {
                fail("Did not expect exception.", throwable);
                return null;
            }));
        bq.take().call();
    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testAsyncReturnVoidException() throws Exception {
        final BlockingQueue<Callable<?>> bq = new LinkedBlockingDeque<>();
        getTestServiceInterface().testAsyncReturnVoid(
            "testAsyncReturnVoidException",
            result -> bq.add(() -> {
                fail("Did not expect result.  Got: " + result);
                return null;
            }),
            throwable -> bq.add(() -> {
                assertTrue(throwable instanceof IllegalArgumentException, "Expected IllegalArgumentException");
                return null;
            }));
        bq.take().call();
    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testAsyncReturnFuture() throws ExecutionException, InterruptedException {
        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("Hello");
        final int result = integerFuture.get();
        assertEquals(result, 42);
    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testAsyncReturnFutureException() throws InterruptedException {

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("testAsyncReturnFuture");

        try {
            integerFuture.get();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException, "Expected IllegalArgumentException but got " + e.getCause());
        }

    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testAsyncReturnFutureWithConsumers() throws Exception {

        final BlockingQueue<Callable<?>> bq = new LinkedBlockingDeque<>();

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture(
            "Hello",
            (Consumer<String>) result -> bq.add(() -> {
                assertEquals("World!", result);
                return null;
            }),
            (Consumer<Throwable>) throwable -> bq.add(() -> {
                fail("Did not expect exception.", throwable);
                return null;
            }));

        bq.take().call();
        assertEquals(integerFuture.get(), Integer.valueOf(42));

    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testAsyncReturnFutureWithConsumersException() throws Exception {

        final BlockingQueue<Callable<?>> bq = new LinkedBlockingDeque<>();

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture(
                "testAsyncReturnFuture",
                (Consumer<String>) result -> bq.add(() -> {
                    fail("Expected instance of IllegalArgumentException.  Got: " + result);
                    return null;
                }),
                (Consumer<Throwable>) throwable -> bq.add(() -> {
                    assertTrue(throwable instanceof IllegalArgumentException, "Expected IllegalArgumentException");
                    return null;
                }));

        bq.take().call();

        try {
            integerFuture.get();
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof IllegalArgumentException, "Expected IllegalArgumentException but got " + ex.getCause());
        }

    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testAsyncReturnFutureWithCustomConsumers() throws Exception {

        final BlockingQueue<Callable<?>> bq = new LinkedBlockingDeque<>();

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture(
                "Hello",
                (TestServiceInterface.MyStringHandler) result -> bq.add(() -> {
                    assertEquals("World!", result);
                    return null;
                }),
                (TestServiceInterface.MyErrorHandler) throwable -> bq.add(() -> {
                    fail("Did not expect exception.", throwable);
                    return null;
                }));

        bq.take().call();
        assertEquals(integerFuture.get(), Integer.valueOf(42));

    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testAsyncReturnFutureWithCustomConsumersException() throws Exception {

        final BlockingQueue<Callable<?>> bq = new LinkedBlockingDeque<>();

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture(
                "testAsyncReturnFuture",
                (TestServiceInterface.MyStringHandler) result -> bq.add(() -> {
                    logger.info("Got response {}", result);
                    fail("Expected instance of IllegalArgumentException.  Got: " + result);
                    return null;
                }),
                (TestServiceInterface.MyErrorHandler) throwable -> bq.add(() -> {
                    logger.info("Got exception", throwable);
                    assertTrue(throwable instanceof IllegalArgumentException, "Expected IllegalArgumentException");
                    return null;
                }));

        bq.take().call();

        try {
            integerFuture.get();
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof IllegalArgumentException, "Expected IllegalArgumentException but got " + ex.getCause());
        }

    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testEcho() {
        final UUID uuid = randomUUID();
        final String result = getTestServiceInterface().testEcho(uuid.toString(), 0.0);
        assertEquals(result, uuid.toString());
    }

    @Test(enabled = false, dependsOnMethods = {"startClient", "startWorkers"}, invocationCount = 1, threadPoolSize = 1)
    public void testEchoWithSomeErrors() {

        final UUID uuid = randomUUID();
        final String result;

        try {
            result = getTestServiceInterface().testEcho(uuid.toString(), 30.0);
            assertEquals(result, uuid.toString());
        } catch (IllegalArgumentException iae) {
            assertEquals(iae.getMessage(), uuid.toString());
        }

    }

    public TestServiceInterface getTestServiceInterface() {
        return testServiceInterface;
    }

    public void setTestServiceInterface(TestServiceInterface testServiceInterface) {
        this.testServiceInterface = testServiceInterface;
    }

    public static class WorkerInstanceModule extends PrivateModule {

        private final ZContext zContext;

        private final ApplicationId applicationId;

        private final List<InstanceId> instanceIdList;

        private final AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService;

        public WorkerInstanceModule(final ZContext zContext,
                                    final AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService,
                                    final ApplicationId applicationId,
                                    final List<InstanceId> instanceIdList) {
            this.zContext = zContext;
            this.asyncConnectionService = asyncConnectionService;
            this.applicationId = applicationId;
            this.instanceIdList = instanceIdList;
        }

        @Override
        protected void configure() {

            bind(new TypeLiteral<AsyncConnectionService<?, ?>>(){}).toInstance(asyncConnectionService);
            bind(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){}).toInstance(asyncConnectionService);

            install(new KryoPayloadReaderWriterModule());

            bind(ZContext.class).toInstance(zContext);

            bind(String.class).annotatedWith(named(JeroMQNode.JEROMQ_NODE_MIN_CONNECTIONS)).toInstance("5");
            bind(String.class).annotatedWith(named(JeroMQNode.JEROMQ_NODE_MAX_CONNECTIONS)).toInstance("25");
            bind(String.class).annotatedWith(named(RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS)).toInstance("5");
            bind(String.class).annotatedWith(named(RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS)).toInstance("25");

            instanceIdList.forEach(i -> install(new PrivateModule() {
                @Override
                protected void configure() {
                    install(new NodeModule(i, instanceIdList, applicationId));
                    expose(Instance.class).annotatedWith(named(i.asString()));
                }
            }));

            instanceIdList.forEach(i -> expose(Instance.class).annotatedWith(named(i.asString())));

            bind(Long.class)
                .annotatedWith(named(REFRESH_RATE_SECONDS))
                .toInstance(DEFAULT_REFRESH_RATE);

            bind(Long.class)
                .annotatedWith(named(REFRESH_TIMEOUT_SECONDS))
                .toInstance(DEFAULT_REFRESH_TIMEOUT);

            bind(Long.class)
                .annotatedWith(named(TOTAL_REFRESH_TIMEOUT_SECONDS))
                .toInstance(DEFAULT_TOTAL_REFRESH_TIMEOUT);

            bind(JeroMQSecurity.class).toInstance(TEST_SECURITY);

        }

    }

    public static class NodeModule extends AbstractModule {

        private final InstanceId instanceId;

        private final ApplicationId applicationId;

        private final List<InstanceId> instanceIdList;

        private final String instanceBindAddress;

        public NodeModule(final InstanceId instanceId,
                          final List<InstanceId> instanceIdList,
                          final ApplicationId applicationId) {
            this.instanceId = instanceId;
            this.instanceIdList = instanceIdList;
            this.applicationId = applicationId;
            this.instanceBindAddress = getBindAddress(instanceId);
        }

        @Override
        protected void configure() {

            final NodeId nodeId = forInstanceAndApplication(instanceId, applicationId);
            final Named nodeNamedAnnotation = named(nodeId.asString());

            install(new JeroMQRemoteInvokerModule());

            install(new JeroMQNodeModule()
                .withNodeId(nodeId)
                .withAnnotation(nodeNamedAnnotation)
                .withDefaultNodeName());

            install(new JeroMQNodeModule()
                .withMasterNodeForInstanceId(instanceId)
                .withDefaultNodeName());

            install(new GuiceServiceLocatorModule());
            install(new JeroMQControlClientModule());
            install(new JeroMQInstanceConnectionServiceModule()
                .withBindAddress(this.instanceBindAddress)
                .withDefaultRefreshInterval());

            install(new StaticInstanceDiscoveryServiceModule().withInstanceAddresses(
                concat(of(instanceBindAddress), instanceIdList
                    .stream()
                    .map(JeroMQEndToEndIntegrationTest::getBindAddress)
                )
                .collect(toList())
            ));

            // Binds the IDs
            bind(InstanceId.class).toInstance(instanceId);
            bind(ApplicationId.class).toInstance(applicationId);

            bind(TestServiceInterface.class).to(IntegrationTestService.class);
            bind(LocalInvocationDispatcher.class).to(ServiceLocatorLocalInvocationDispatcher.class);
            bind(RemoteInvocationDispatcher.class).to(SimpleRemoteInvocationDispatcher.class);

            bind(SimpleRemoteInvokerRegistry.class).asEagerSingleton();
            bind(RemoteInvokerRegistry.class).to(SimpleRemoteInvokerRegistry.class);

            bind(String.class)
                    .annotatedWith(named(REMOTE_SCOPE))
                    .toInstance(MASTER_SCOPE);

            bind(String.class)
                    .annotatedWith(named(REMOTE_PROTOCOL))
                    .toInstance(ELEMENTS_RT_PROTOCOL);

            final Random threadLocalRandom = ThreadLocalRandom.current();
            final InstanceMetadataContext mock = mock(InstanceMetadataContext.class);
            bind(InstanceMetadataContext.class).toInstance(mock);

            when(mock.getNodeIds()).thenAnswer(i -> {
                logger.info("Reporting NodeID {} for instance {}", nodeId, instanceId);
                return singleton(nodeId);
            });

            when(mock.getInstanceQuality()).thenAnswer(i -> {
                final double quality = threadLocalRandom.nextDouble();
                logger.info("Reporting quality {} for instance {}", quality, instanceId);
                return quality;
            });

            when(mock.getInstanceMetadataAsync(any(), any())).thenAnswer(i -> {

                final Consumer<InstanceMetadata> success = i.getArgument(0);

                final double quality = threadLocalRandom.nextDouble();
                final Set<NodeId> nodeIdSet = singleton(nodeId);

                final var metadata = new InstanceMetadata();
                metadata.setQuality(quality);
                metadata.setNodeIds(nodeIdSet);
                success.accept(metadata);

                return AsyncOperation.DEFAULT;

            });

            bind(NodeLifecycle.class).toInstance(new NodeLifecycle() {
                @Override
                public void nodePreStart(Node node) {
                    logger.info("Starting test node lifecycle.");
                }

                @Override
                public void nodePostStop(Node node) {
                    logger.info("Shutting down test node lifecycle.");
                }
            });

            final Provider<Node> nodeProvider = getProvider(Key.get(Node.class, nodeNamedAnnotation));
            bind(new TypeLiteral<Set<Node>>(){}).toProvider(() -> singleton(nodeProvider.get()));

            // Sets up the worker instance
            final PersistenceEnvironment pMock = mock(PersistenceEnvironment.class);
            bind(PersistenceEnvironment.class).toInstance(pMock);

            install(new SimpleExecutorsModule().withDefaultSchedulerThreads());

            bind(SimpleWorkerInstance.class).asEagerSingleton();
            bind(Worker.class).to(SimpleWorkerInstance.class);
            bind(Node.Factory.class).toInstance(Node.Factory.unsupported());
            bind(Instance.class).annotatedWith(named(instanceId.asString())).to(SimpleWorkerInstance.class);

        }

    }

    public static class ClientModule extends AbstractModule {

        private final ZContext zContext;

        private final InstanceId instanceId;

        private final ApplicationId applicationId;

        private final List<InstanceId> instanceIdList;

        private final String instanceBindAddress;

        private final AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService;

        public ClientModule(final ZContext zContext,
                            final AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService,
                            final ApplicationId applicationId,
                            final List<InstanceId> instanceIdList) {
            this.zContext = zContext;
            this.asyncConnectionService = asyncConnectionService;
            this.applicationId = applicationId;
            this.instanceId = randomInstanceId();
            this.instanceIdList = instanceIdList;
            this.instanceBindAddress = getBindAddress(instanceId);
        }

        @Override
        protected void configure() {

            install(new GuiceServiceLocatorModule());
            install(new KryoPayloadReaderWriterModule());
            install(new JeroMQRemoteInvokerModule());
            install(new JeroMQControlClientModule());
            install(new JeroMQInstanceConnectionServiceModule()
                .withBindAddress(this.instanceBindAddress)
                .withDefaultRefreshInterval());

            install(new StaticInstanceDiscoveryServiceModule().withInstanceAddresses(
                concat(of(instanceBindAddress), instanceIdList
                        .stream()
                        .map(JeroMQEndToEndIntegrationTest::getBindAddress)
                    )
                .collect(toList())
            ));

            bind(new TypeLiteral<AsyncConnectionService<?, ?>>(){}).toInstance(asyncConnectionService);
            bind(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){}).toInstance(asyncConnectionService);

            bind(RemoteInvocationDispatcher.class).to(SimpleRemoteInvocationDispatcher.class);
            bind(RemoteInvokerRegistry.class).to(SimpleRemoteInvokerRegistry.class).asEagerSingleton();

            bind(InstanceId.class).toInstance(instanceId);
            bind(ApplicationId.class).toInstance(applicationId);

            bind(ZContext.class).toInstance(zContext);
            bind(TestServiceInterface.class).toProvider(new RemoteProxyProvider<>(TestServiceInterface.class));

            bind(Instance.class).to(SimpleInstance.class).asEagerSingleton();

            bind(String.class).annotatedWith(named(RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS)).toInstance("5");
            bind(String.class).annotatedWith(named(RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS)).toInstance("25");

            bind(Long.class)
                .annotatedWith(named(REFRESH_RATE_SECONDS))
                .toInstance(DEFAULT_REFRESH_RATE);

            bind(Long.class)
                .annotatedWith(named(REFRESH_TIMEOUT_SECONDS))
                .toInstance(DEFAULT_REFRESH_TIMEOUT);

            bind(Long.class)
                .annotatedWith(named(TOTAL_REFRESH_TIMEOUT_SECONDS))
                .toInstance(DEFAULT_TOTAL_REFRESH_TIMEOUT);

            bind(JeroMQSecurity.class).toInstance(TEST_SECURITY);

        }

    }

    private static String getBindAddress(final InstanceId instanceId) {
        final String str = instanceId.asString();
        return format("inproc://instance/%s", str);
    }

}

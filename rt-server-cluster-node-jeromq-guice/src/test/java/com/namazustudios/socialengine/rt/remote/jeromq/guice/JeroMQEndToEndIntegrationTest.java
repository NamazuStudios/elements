package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.remote.TestServiceInterface;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.IoCInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.RemoteProxyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zeromq.ZContext;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.*;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.CONNECT_ADDRESS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.TIMEOUT;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

public class JeroMQEndToEndIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQEndToEndIntegrationTest.class);

    private Node node;

    private RemoteInvoker remoteInvoker;

    private TestServiceInterface testServiceInterface;

    @BeforeClass
    public void setup() {

        final ZContext zContext = new ZContext();

        final Injector nodeInjector = Guice.createInjector(new NodeModule(zContext));
        final Injector clientInvoker = Guice.createInjector(new ClientModule(zContext));

        setNode(nodeInjector.getInstance(Node.class));

        setRemoteInvoker(clientInvoker.getInstance(RemoteInvoker.class));
        setTestServiceInterface(clientInvoker.getInstance(TestServiceInterface.class));

        getNode().start();
        getRemoteInvoker().start();

    }

    @AfterClass
    public void shutdown() {
        getRemoteInvoker().stop();
        getNode().stop();
    }

    public Node getNode() {
        return node;
    }

    @Test(invocationCount = 10, threadPoolSize = 10)
    public void testRemoteInvokeSync() {
        getTestServiceInterface().testSyncVoid("Hello");
    }

    @Test(invocationCount = 10, threadPoolSize = 10, expectedExceptions = IllegalArgumentException.class)
    public void testRemoteInvokeSyncException() {
        getTestServiceInterface().testSyncVoid("testSyncVoid");
    }

    @Test(invocationCount = 10, threadPoolSize = 10)
    public void testRemoteInvokeSyncReturn() {
        final double result = getTestServiceInterface().testSyncReturn("Hello");
        assertEquals(result, 40.42);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoteInvokeSyncReturnException() {
        getTestServiceInterface().testSyncReturn("testSyncReturn");
        fail("Did not expect return.");
    }

    @Test(invocationCount = 10, threadPoolSize = 10)
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

    @Test(invocationCount = 10, threadPoolSize = 10)
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

    @Test(invocationCount = 10, threadPoolSize = 10)
    public void testAsyncReturnFuture() throws ExecutionException, InterruptedException {
        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("Hello");
        final int result = integerFuture.get();
        assertEquals(result, 42);
    }

    @Test(invocationCount = 10, threadPoolSize = 10)
    public void testAsyncReturnFutureException() throws InterruptedException {

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("testAsyncReturnFuture");

        try {
            integerFuture.get();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException, "Expected IllegalArgumentException but got " + e.getCause());
        }

    }

    @Test(invocationCount = 10, threadPoolSize = 10)
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

    @Test(invocationCount = 10, threadPoolSize = 10)
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

    @Test(invocationCount = 10, threadPoolSize = 10)
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

    @Test(invocationCount = 10, threadPoolSize = 10)
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

    @Test(invocationCount = 10, threadPoolSize = 10)
    public void testEcho() {
        final UUID uuid = randomUUID();
        final String result = getTestServiceInterface().testEcho(uuid.toString(), 0.0);
        assertEquals(result, uuid.toString());
    }

    @Test(invocationCount = 10, threadPoolSize = 10)
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

    public void setNode(Node node) {
        this.node = node;
    }

    public RemoteInvoker getRemoteInvoker() {
        return remoteInvoker;
    }

    public void setRemoteInvoker(RemoteInvoker remoteInvoker) {
        this.remoteInvoker = remoteInvoker;
    }

    public TestServiceInterface getTestServiceInterface() {
        return testServiceInterface;
    }

    public void setTestServiceInterface(TestServiceInterface testServiceInterface) {
        this.testServiceInterface = testServiceInterface;
    }

    public static class NodeModule extends AbstractModule {

        private final ZContext zContext;

        public NodeModule(ZContext zContext) {
            this.zContext = zContext;
        }

        @Override
        protected void configure() {

            install(new JeroMQNodeModule());

            bind(ZContext.class).toInstance(zContext);

            bind(String.class).annotatedWith(named(TIMEOUT)).toInstance("60");
            bind(String.class).annotatedWith(named(MIN_CONNECTIONS)).toInstance("5");

            bind(IocResolver.class).to(GuiceIoCResolver.class);
            bind(TestServiceInterface.class).to(IntegrationTestService.class);
            bind(InvocationDispatcher.class).to(IoCInvocationDispatcher.class);

            bind(String.class).annotatedWith(named(NUMBER_OF_DISPATCHERS)).toInstance("100");
            bind(String.class).annotatedWith(named(BIND_ADDRESS)).toInstance("inproc://integration-test");

            bind(String.class).annotatedWith(named(ID)).toInstance("integration-test");
            bind(String.class).annotatedWith(named(NAME)).toInstance("integration-test");

        }

    }

    public static class ClientModule extends AbstractModule {

        private final ZContext zContext;

        public ClientModule(ZContext zContext) {
            this.zContext = zContext;
        }

        @Override
        protected void configure() {

            install(new JeroMQRemoteInvokerModule());

            bind(ZContext.class).toInstance(zContext);

            bind(String.class).annotatedWith(named(TIMEOUT)).toInstance("10");
            bind(String.class).annotatedWith(named(MIN_CONNECTIONS)).toInstance("100");

            bind(String.class)
                .annotatedWith(named(CONNECT_ADDRESS))
                .toInstance("inproc://integration-test");

            bind(TestServiceInterface.class)
                .toProvider(new RemoteProxyProvider<>(TestServiceInterface.class));

        }

    }

}

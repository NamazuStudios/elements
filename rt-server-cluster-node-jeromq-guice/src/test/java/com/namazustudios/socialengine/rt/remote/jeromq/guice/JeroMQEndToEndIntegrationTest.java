package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.remote.TestServiceInterface;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.annotation.ErrorHandler;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.annotation.ResultHandler;
import com.namazustudios.socialengine.rt.annotation.Serialize;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.IoCInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.RemoteProxyProvider;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zeromq.ZContext;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.BIND_ADDRESS;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.NUMBER_OF_DISPATCHERS;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.NODE_ADDRESS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.TIMEOUT;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

public class JeroMQEndToEndIntegrationTest {

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

    @Test
    public void testRemoteInvokeSync() {
        getTestServiceInterface().testSyncVoid("Hello");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoteInvokeSyncException() {
        getTestServiceInterface().testSyncVoid("testSyncVoid");
    }

    @Test
    public void testRemoteInvokeSyncReturn() {
        final double result = getTestServiceInterface().testSyncReturn("Hello");
        assertEquals(result, 40.42);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoteInvokeSyncReturnException() {
        getTestServiceInterface().testSyncReturn("testSyncReturn");
        fail("Did not expect return.");
    }

    @Test
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

    @Test
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

    @Test
    public void testAsyncReturnFuture() throws ExecutionException, InterruptedException {
        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("Hello");
        final int result = integerFuture.get();
        assertEquals(result, 42);
    }

    @Test
    public void testAsyncReturnFutureException() throws InterruptedException {

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("testAsyncReturnFuture");

        try {
            integerFuture.get();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException, "Expected IllegalArgumentException but got " + e.getCause());
        }

    }

//    @Test
//    public void testAsyncReturnFutureWithConsumers() throws Exception {
//
//        final BlockingQueue<Callable<?>> bq = new LinkedBlockingDeque<>();
//
//        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture(
//            "Hello",
//            (Consumer<String>) result -> bq.add(() -> {
//                assertEquals("World!", result);
//                return null;
//            }),
//            (Consumer<Throwable>) throwable -> bq.add(() -> {
//                fail("Did not expect exception.", throwable);
//                return null;
//            }));
//
//        bq.take().call();
//        assertEquals(integerFuture.get(), Integer.valueOf(42));
//
//    }
//
//    @Test
//    public void testAsyncReturnFutureWithConsumersException() throws Exception {
//
//        final BlockingQueue<Callable<?>> bq = new LinkedBlockingDeque<>();
//
//        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture(
//                "testAsyncReturnFuture",
//                (Consumer<String>) result -> bq.add(() -> {
//                    fail("Expected instance of IllegalArgumentException.  Got: " + result);
//                    return null;
//                }),
//                (Consumer<Throwable>) throwable -> bq.add(() -> {
//                    assertTrue(throwable instanceof IllegalArgumentException, "Expected IllegalArgumentException");
//                    return null;
//                }));
//
//        bq.take().call();
//        assertEquals(integerFuture.get(), Integer.valueOf(42));
//    }

    @Test
    public void testAsyncReturnFutureWithCustomConsumers() {

    }

    @Test
    public void testAsyncReturnFutureWithCustomConsumersException() {

    }

//    @RemotelyInvokable
//    Future<Integer> testAsyncReturnFuture(@Serialize String msg,
//                                          @ResultHandler Consumer<String> stringConsumer,
//                                          @ErrorHandler Consumer<Throwable> throwableConsumer);
//
//    @RemotelyInvokable
//    Future<Integer> testAsyncReturnFuture(@Serialize String msg,
//                                          @ResultHandler TestServiceInterface.MyStringHandler stringConsumer,
//                                          @ErrorHandler TestServiceInterface.MyErrorHandler errorHandler);
//
    @Test(invocationCount = 40, threadPoolSize = 40)
    public void testEcho() {
        final UUID uuid = randomUUID();
        final String result = getTestServiceInterface().testEcho(uuid.toString(), 0.0);
        assertEquals(result, uuid.toString());
    }

    @Test(invocationCount = 40, threadPoolSize = 40)
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

            bind(String.class).annotatedWith(named(NUMBER_OF_DISPATCHERS)).toInstance("5");
            bind(String.class).annotatedWith(named(BIND_ADDRESS)).toInstance("inproc://integration-test");

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
                .annotatedWith(named(NODE_ADDRESS))
                .toInstance("inproc://integration-test");

            bind(TestServiceInterface.class)
                .toProvider(new RemoteProxyProvider<>(TestServiceInterface.class));

        }

    }

}

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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zeromq.ZContext;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.BIND_ADDRESS;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQNode.NUMBER_OF_DISPATCHERS;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.*;
import static com.namazustudios.socialengine.rt.jeromq.CachedConnectionPool.MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.jeromq.CachedConnectionPool.TIMEOUT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

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
        getTestServiceInterface().testSyncVoid("World");
    }

    @Test
    public void testRemoteInvokeSyncReturn() {
        final double result = getTestServiceInterface().testSyncReturn("Hello");
        assertEquals(result, 40.42);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoteInvokeSyncReturnException() {
        getTestServiceInterface().testSyncVoid("World");
        fail("Did not expect return.");
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

            bind(String.class).annotatedWith(named(TIMEOUT)).toInstance("60");
            bind(String.class).annotatedWith(named(MIN_CONNECTIONS)).toInstance("5");

            bind(String.class)
                .annotatedWith(named(NODE_ADDRESS))
                .toInstance("inproc://integration-test");

            bind(TestServiceInterface.class)
                .toProvider(new RemoteProxyProvider<>(TestServiceInterface.class));

        }

    }

}

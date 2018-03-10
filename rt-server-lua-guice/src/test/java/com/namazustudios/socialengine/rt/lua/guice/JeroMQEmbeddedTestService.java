package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQClientModule;
import org.zeromq.ZContext;

import static org.zeromq.ZContext.shadow;

/**
 * Embeds a test kit which supplies an instance of {@link Context} and {@link Node}.
 */
public class JeroMQEmbeddedTestService implements AutoCloseable {

    private static final String INTERNAL_NODE_ADDRESS = "inproc://integration-test";

    private final Node node;

    private final Context context;

    public JeroMQEmbeddedTestService() {

        final ZContext zContext = new ZContext();

        final Injector nodeInjector = Guice.createInjector(new TestJeroMQLuaNodeModule()
                .withZContext(shadow(zContext))
                .withBindAddress(INTERNAL_NODE_ADDRESS)
                .withNodeId("integration-test-node")
                .withNodeName("integration-test-node")
                .withMinimumConnections(5)
                .withMaximumConnections(250)
                .withTimeout(60)
                .withNumberOfDispatchers(10));

        final Injector clientInjector = Guice.createInjector(new JeroMQClientModule()
                .withZContext(shadow(zContext))
                .withConnectAddress(INTERNAL_NODE_ADDRESS)
                .withMinimumConnections(5)
                .withMaximumConnections(250)
                .withTimeout(60));

        node = nodeInjector.getInstance(Node.class);
        context = clientInjector.getInstance(Context.class);

    }

    public JeroMQEmbeddedTestService start() {
        getNode().start();
        getContext().start();
        return this;
    }

    public Node getNode() {
        return node;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void close() {
        getContext().shutdown();
        getNode().stop();
    }

}

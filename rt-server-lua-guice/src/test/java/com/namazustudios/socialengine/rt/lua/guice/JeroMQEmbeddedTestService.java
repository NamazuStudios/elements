package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQClientModule;
import org.zeromq.ZContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;
import static jdk.nashorn.internal.objects.NativeFunction.bind;
import static org.zeromq.ZContext.shadow;

/**
 * Embeds a test kit which supplies an instance of {@link Context} and {@link Node}.
 */
public class JeroMQEmbeddedTestService implements AutoCloseable {

    private static final String INTERNAL_NODE_ADDRESS = "inproc://integration-test";

    private Node node;

    private Context context;

    private List<Module> nodeModules = new ArrayList<>(); {
        nodeModules.add(binder -> binder.bind(TestJavaModule.class));
    }

    private List<Module> clientModules = new ArrayList<>();

    public JeroMQEmbeddedTestService() {}

    public JeroMQEmbeddedTestService withNodeModule(final Module module) {
        nodeModules.add(module);
        return this;
    }

    public JeroMQEmbeddedTestService withClientModule(final Module module) {
        clientModules.add(module);
        return this;
    }

    public JeroMQEmbeddedTestService withDefaultHttpClient() {
        return withNodeModule(binder -> binder.bind(Client.class).toProvider(ClientBuilder::newClient).asEagerSingleton());
    }

    public JeroMQEmbeddedTestService start() {

        final ZContext zContext = new ZContext();

        final Injector nodeInjector = Guice.createInjector(new TestJeroMQNodeModule()
                .withNodeModules(nodeModules)
                .withZContext(shadow(zContext))
                .withBindAddress(INTERNAL_NODE_ADDRESS)
                .withNodeId("integration-test-node")
                .withNodeName("integration-test-node")
                .withMinimumConnections(5)
                .withMaximumConnections(250)
                .withTimeout(60)
                .withHandlerTimeout(3, MINUTES)
                .withSchedulerThreads(1));

        final List<Module> clientModules = new ArrayList<>(this.clientModules);

        clientModules.add(new JeroMQClientModule()
            .withZContext(shadow(zContext))
            .withConnectAddress(INTERNAL_NODE_ADDRESS)
            .withMinimumConnections(5)
            .withMaximumConnections(250)
            .withTimeout(60));

        final Injector clientInjector = Guice.createInjector(clientModules);

        node = nodeInjector.getInstance(Node.class);
        context = clientInjector.getInstance(Context.class);

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

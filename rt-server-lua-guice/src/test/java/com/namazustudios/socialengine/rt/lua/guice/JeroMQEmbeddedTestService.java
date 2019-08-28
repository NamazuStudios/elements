package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQContextModule;
import org.zeromq.ZContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.rt.id.ApplicationId.randomApplicationId;
import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static org.zeromq.ZContext.shadow;

/**
 * Embeds a test kit which supplies an instance of {@link Context} and {@link Node}.
 */
public class JeroMQEmbeddedTestService implements AutoCloseable {

    private static final String INTERNAL_NODE_ADDRESS = "inproc://integration-test";

    private Node node;

    private Context context;

    private List<Module> nodeModules = new ArrayList<>();

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
        final InstanceId instanceId = randomInstanceId();

        final Injector nodeInjector = Guice.createInjector(new TestJeroMQNodeModule()
            .withNodeModules(nodeModules)
            .withZContext(shadow(zContext))
            .withNodeName("integration-test-node")
            .withMinimumConnections(5)
            .withMaximumConnections(250));

        final List<Module> clientModules = new ArrayList<>(this.clientModules);

        clientModules.add(new JeroMQContextModule()
            .withZContext(shadow(zContext))
        );

        final Injector clientInjector = Guice.createInjector(clientModules);

        node = nodeInjector.getInstance(Node.class);
        context = clientInjector.getInstance(Context.class);

// TODO
//        getNode().start(binding);

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

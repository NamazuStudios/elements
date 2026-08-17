package dev.getelements.elements.cluster.fabric.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.cluster.fabric.FabricEndpoint;
import dev.getelements.elements.cluster.fabric.FabricRemoteInvoker;
import dev.getelements.elements.rt.kryo.guice.KryoPayloadReaderWriterModule;
import dev.getelements.elements.rt.remote.Invocation;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer.configure;
import static org.testng.Assert.assertEquals;

/**
 * Proves the Fabric WebSocket transport prototype (issue #10) round-trips a single {@link Invocation} between a
 * real Jetty server hosting {@link FabricEndpoint} and a {@link FabricRemoteInvoker} client, dispatching against
 * the existing (deprecated) {@code ServiceLocatorLocalInvocationDispatcher} &mdash; this is the decision-gate test:
 * if this passes cleanly, Fabric continues as the cluster RPC transport.
 */
public class FabricWsRoundTripIntegrationTest {

    private static final String CLUSTER_WS_PATH = "/cluster/v1";

    private Server server;

    private FabricRemoteInvoker fabricRemoteInvoker;

    @BeforeClass
    public void startServer() throws Exception {

        final var injector = Guice.createInjector(
                new KryoPayloadReaderWriterModule(),
                new FabricGuiceModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(FabricTestService.class).to(FabricTestServiceImpl.class);
                    }
                }
        );

        final var fabricEndpoint = injector.getInstance(FabricEndpoint.class);
        fabricRemoteInvoker = injector.getInstance(FabricRemoteInvoker.class);

        server = new Server();

        final var connector = new ServerConnector(server);
        connector.setPort(0);
        server.addConnector(connector);

        final var handler = new ServletContextHandler();
        handler.setContextPath("/");
        server.setHandler(handler);

        configure(handler, (servletContext, serverContainer) -> {
            final var config = ServerEndpointConfig.Builder
                    .create(FabricEndpoint.class, CLUSTER_WS_PATH)
                    .configurator(new ServerEndpointConfig.Configurator() {
                        @Override
                        public <T> T getEndpointInstance(final Class<T> endpointClass) {
                            return endpointClass.cast(fabricEndpoint);
                        }
                    })
                    .build();
            serverContainer.addEndpoint(config);
        });

        server.start();

    }

    @AfterClass
    public void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testRoundTrip() throws Exception {

        final var port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        final var uri = URI.create("ws://localhost:" + port + CLUSTER_WS_PATH);

        final var invocation = new Invocation();
        invocation.setType(FabricTestService.class.getName());
        invocation.setMethod("add");
        invocation.setParameters(new ArrayList<>(asList(double.class.getName(), double.class.getName())));
        invocation.setArguments(new ArrayList<>(asList(1.5, 2.5)));

        final var result = fabricRemoteInvoker.invoke(uri, invocation);

        assertEquals(result.getResult(), 4.0);

    }

}

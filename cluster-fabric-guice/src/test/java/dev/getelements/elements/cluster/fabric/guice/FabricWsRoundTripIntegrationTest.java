package dev.getelements.elements.cluster.fabric.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.cluster.fabric.FabricEndpoint;
import dev.getelements.elements.cluster.fabric.JakartaWebsocketRemoteInvoker;
import dev.getelements.elements.rt.kryo.guice.KryoPayloadReaderWriterModule;
import dev.getelements.elements.rt.remote.ProxyBuilder;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.guice.GuiceServiceLocator;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer.configure;
import static org.testng.Assert.assertEquals;

/**
 * Proves the Fabric WebSocket transport (issue #10) round-trips a real {@code @RemotelyInvokable} call between a
 * real Jetty server hosting {@link FabricEndpoint} and a {@link JakartaWebsocketRemoteInvoker} client, dispatching
 * against the existing (deprecated) {@code ServiceLocatorLocalInvocationDispatcher} &mdash; this is the
 * decision-gate test: if this passes cleanly, Fabric continues as the cluster RPC transport.
 */
public class FabricWsRoundTripIntegrationTest {

    private static final String CLUSTER_WS_PATH = "/cluster/v1";

    private Server server;

    private JakartaWebsocketRemoteInvoker jakartaWebsocketRemoteInvoker;

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
        jakartaWebsocketRemoteInvoker = injector.getInstance(JakartaWebsocketRemoteInvoker.class);

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
    public void testRoundTrip() {

        final var port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        final var uri = "ws://localhost:" + port + CLUSTER_WS_PATH;

        jakartaWebsocketRemoteInvoker.start(uri, 30, TimeUnit.SECONDS);

        final var proxy = new ProxyBuilder<>(FabricTestService.class)
                .dontProxyDefaultMethods()
                .withDefaultHashCodeAndEquals()
                .withHandlersForRemoteInvoker(jakartaWebsocketRemoteInvoker)
                .build();

        final var result = proxy.add(1.5, 2.5);

        assertEquals(result, 4.0);

    }

}

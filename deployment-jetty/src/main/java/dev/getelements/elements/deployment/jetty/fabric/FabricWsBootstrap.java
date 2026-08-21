package dev.getelements.elements.deployment.jetty.fabric;

import dev.getelements.elements.cluster.fabric.FabricEndpoint;
import dev.getelements.elements.deployment.jetty.loader.HttpPathRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer.configure;

/**
 * Registers the Fabric WebSocket transport prototype's {@code /cluster/v1} endpoint exactly once, unconditionally,
 * at injector-creation time. Unlike {@link dev.getelements.elements.deployment.jetty.loader.JakartaWebsocketLoader},
 * this is not tied to any Element deployment lifecycle &mdash; it's a single, static, instance-wide endpoint (issue
 * #10).
 */
public class FabricWsBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(FabricWsBootstrap.class);

    /**
     * The name under which the {@link Handler.Sequence} that this class populates is bound.
     */
    public static final String HANDLER_SEQUENCE = "dev.getelements.elements.cluster.fabric.handler.ws";

    /**
     * The fixed path of the Fabric endpoint, identical on every instance.
     */
    public static final String CLUSTER_WS_PATH = FabricEndpoint.CLUSTER_WS_PATH;

    @Inject
    public FabricWsBootstrap(@Named(HANDLER_SEQUENCE) final Handler.Sequence sequence,
                              final HttpPathRegistry httpPathRegistry,
                              final FabricEndpoint fabricEndpoint) {

        if (!httpPathRegistry.register(CLUSTER_WS_PATH)) {
            logger.warn("WARNING: Fabric WebSocket path '{}' is already registered by another element or the " +
                    "system. Fabric will still be deployed but may conflict.", CLUSTER_WS_PATH);
        }

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");

        configure(servletContextHandler, (servletContext, serverContainer) -> {
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

        sequence.addHandler(servletContextHandler);

        try {
            servletContextHandler.start();
            logger.info("Registered Fabric WebSocket endpoint at {}.", CLUSTER_WS_PATH);
        } catch (Exception ex) {
            sequence.removeHandler(servletContextHandler);
            throw new IllegalStateException("Failed to start Fabric WebSocket endpoint.", ex);
        }

    }

}

package dev.getelements.elements.rpc.guice;

import dev.getelements.elements.guice.BaseServletModule;
import dev.getelements.elements.rpc.RpcResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Map;

public class RpcJerseyModule extends BaseServletModule {
    @Override
    protected void configureServlets() {

        // Setup servlets
        bind(ServletContainer.class).asEagerSingleton();

        final var params = Map.of("jakarta.ws.rs.Application", RpcResourceConfig.class.getName());

        serve("/*").with(ServletContainer.class, params);
        useStandardSecurityFilters();

    }
}

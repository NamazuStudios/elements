package dev.getelements.elements.rpc.guice;

import com.google.inject.servlet.ServletModule;
import dev.getelements.elements.rpc.RpcResourceConfig;
import dev.getelements.elements.servlet.security.HttpServletCORSFilter;
import dev.getelements.elements.servlet.security.HttpServletGlobalSecretHeaderFilter;
import dev.getelements.elements.servlet.security.HttpServletSessionIdAuthenticationFilter;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Map;

public class RpcJerseyModule extends ServletModule {
    @Override
    protected void configureServlets() {

        // Setup servlets
        bind(ServletContainer.class).asEagerSingleton();
        bind(HttpServletCORSFilter.class).asEagerSingleton();
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
        bind(HttpServletSessionIdAuthenticationFilter.class).asEagerSingleton();

        final var params = Map.of("javax.ws.rs.Application", RpcResourceConfig.class.getName());

        serve("/*").with(ServletContainer.class, params);
        filter("/*").through(HttpServletCORSFilter.class);
        filter("/*").through(HttpServletGlobalSecretHeaderFilter.class);
        filter("/*").through(HttpServletSessionIdAuthenticationFilter.class);

    }
}

package com.namazustudios.socialengine.rpc.guice;

import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.rpc.RpcResourceConfig;
import com.namazustudios.socialengine.service.SessionService;
import com.namazustudios.socialengine.service.auth.DefaultSessionService;
import com.namazustudios.socialengine.servlet.security.HttpServletCORSFilter;
import com.namazustudios.socialengine.servlet.security.HttpServletGlobalSecretHeaderFilter;
import com.namazustudios.socialengine.servlet.security.HttpServletSessionIdAuthenticationFilter;
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

package dev.getelements.elements.cdnserve.guice;

import com.google.inject.servlet.ServletModule;
import dev.getelements.elements.service.SessionService;
import dev.getelements.elements.service.auth.DefaultSessionService;
import dev.getelements.elements.servlet.security.HttpServletCORSFilter;
import dev.getelements.elements.servlet.security.HttpServletGlobalSecretHeaderFilter;
import dev.getelements.elements.servlet.security.HttpServletSessionIdAuthenticationFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CdnJerseyModule extends ServletModule {

    private static final Logger logger = LoggerFactory.getLogger(CdnJerseyModule.class);

    @Override
    protected final void configureServlets() {

        // Setup servlets

        bind(ServletContainer.class).asEagerSingleton();
        bind(HttpServletCORSFilter.class).asEagerSingleton();
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
        bind(HttpServletSessionIdAuthenticationFilter.class).asEagerSingleton();

        bind(SessionService.class).to(DefaultSessionService.class);

        final var params = Map.of("javax.ws.rs.Application", CdnGuiceResourceConfig.class.getName());

        serve("/*").with(ServletContainer.class, params);
        filter("/*").through(HttpServletCORSFilter.class);
        filter("/*").through(HttpServletGlobalSecretHeaderFilter.class);
        filter("/*").through(HttpServletSessionIdAuthenticationFilter.class);

    }

}

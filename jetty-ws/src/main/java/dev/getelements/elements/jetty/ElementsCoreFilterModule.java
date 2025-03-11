package dev.getelements.elements.jetty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.PrivateModule;
import dev.getelements.elements.servlet.security.*;

public class ElementsCoreFilterModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(ObjectMapper.class).asEagerSingleton();

        bind(HttpServletCORSFilter.class).asEagerSingleton();
        bind(HttpServletBasicAuthFilter.class).asEagerSingleton();
        bind(HttpServletBearerAuthenticationFilter.class).asEagerSingleton();
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
        bind(HttpServletSessionIdAuthenticationFilter.class).asEagerSingleton();

        expose(HttpServletCORSFilter.class);
        expose(HttpServletBasicAuthFilter.class);
        expose(HttpServletBearerAuthenticationFilter.class);
        expose(HttpServletGlobalSecretHeaderFilter.class);
        expose(HttpServletSessionIdAuthenticationFilter.class);

    }

}

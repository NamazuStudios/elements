package dev.getelements.elements.jetty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.PrivateModule;
import dev.getelements.elements.servlet.security.*;

/**
    * This module binds the core filters used in the Elements web service.
    * It includes filters for CORS, basic authentication, service scope, bearer authentication,
    * global secret header, and session ID authentication.
    *
    * @see HttpServletCORSFilter
    * @see HttpServletBasicAuthFilter
    * @see HttpServletElementScopeFilter
    * @see HttpServletBearerAuthenticationFilter
    * @see HttpServletGlobalSecretHeaderFilter
    * @see HttpServletSessionIdAuthenticationFilter
 */
public class ElementsCoreFilterModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(ObjectMapper.class).asEagerSingleton();

        bind(HttpServletCORSFilter.class).asEagerSingleton();
        bind(HttpServletBasicAuthFilter.class).asEagerSingleton();
        bind(HttpServletElementScopeFilter.class).asEagerSingleton();
        bind(HttpServletBearerAuthenticationFilter.class).asEagerSingleton();
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
        bind(HttpServletSessionIdAuthenticationFilter.class).asEagerSingleton();
        bind(HttpServletHeaderProfileOverrideFilter.class).asEagerSingleton();

        expose(HttpServletCORSFilter.class);
        expose(HttpServletBasicAuthFilter.class);
        expose(HttpServletElementScopeFilter.class);
        expose(HttpServletBearerAuthenticationFilter.class);
        expose(HttpServletGlobalSecretHeaderFilter.class);
        expose(HttpServletSessionIdAuthenticationFilter.class);
        expose(HttpServletHeaderProfileOverrideFilter.class);

    }

}

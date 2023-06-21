package dev.getelements.elements.guice;

import com.google.inject.servlet.ServletModule;
import dev.getelements.elements.servlet.security.*;

public abstract class BaseServletModule extends ServletModule {

    protected void useHttpBasicSecurityFilters() {
        useHttpBasicSecurityFilters("/*");
    }

    protected void useHttpBasicSecurityFilters(final String pattern) {
        bind(HttpServletBasicAuthFilter.class).asEagerSingleton();
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
        filter(pattern).through(HttpServletGlobalSecretHeaderFilter.class);
        filter(pattern).through(HttpServletBasicAuthFilter.class);
    }

    protected void useStandardSecurityFilters() {
        useStandardSecurityFilters("/*");
    }

    protected void useStandardSecurityFilters(final String pattern) {

        bind(HttpServletCORSFilter.class).asEagerSingleton();
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
        bind(HttpServletBearerAuthenticationFilter.class).asEagerSingleton();
        bind(HttpServletSessionIdAuthenticationFilter.class).asEagerSingleton();

        filter(pattern).through(HttpServletCORSFilter.class);
        filter(pattern).through(HttpServletGlobalSecretHeaderFilter.class);
        filter(pattern).through(HttpServletBearerAuthenticationFilter.class);
        filter(pattern).through(HttpServletSessionIdAuthenticationFilter.class);

    }

}

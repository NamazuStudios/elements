package dev.getelements.elements.formidium;

import com.google.inject.servlet.ServletModule;
import dev.getelements.elements.guice.BaseServletModule;
import dev.getelements.elements.servlet.security.HttpServletBearerAuthenticationFilter;
import dev.getelements.elements.servlet.security.HttpServletCORSFilter;
import dev.getelements.elements.servlet.security.HttpServletGlobalSecretHeaderFilter;
import dev.getelements.elements.servlet.security.HttpServletSessionIdAuthenticationFilter;

import java.util.Map;

import static dev.getelements.elements.servlet.security.HttpServletCORSFilter.INTERCEPT;

public class FormidiumServletModule extends BaseServletModule {

    private final String formidiumApiUrl;

    public FormidiumServletModule(final String formidiumApiUrl) {
        this.formidiumApiUrl = formidiumApiUrl;
    }

    @Override
    protected void configureServlets() {

        bind(FormidiumProxyServlet.class).asEagerSingleton();

        final var params = Map.of(
                "prefix", "/",
                "proxyTo", formidiumApiUrl
        );

        serve("/*").with(FormidiumProxyServlet.class, Map.of(
                "prefix", "/",
                "proxyTo", formidiumApiUrl
        ));

        filter("/*").through(HttpServletCORSFilter.class, Map.of(
                INTERCEPT, "true"
        ));

        useStandardSecurityFilters();
        
    }

}

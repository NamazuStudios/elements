package dev.getelements.elements.formidium;

import com.google.inject.servlet.ServletModule;
import dev.getelements.elements.servlet.security.HttpServletCORSFilter;
import dev.getelements.elements.servlet.security.HttpServletGlobalSecretHeaderFilter;
import dev.getelements.elements.servlet.security.HttpServletSessionIdAuthenticationFilter;

import java.util.Map;

import static dev.getelements.elements.servlet.security.HttpServletCORSFilter.INTERCEPT;

public class FormidiumServletModule extends ServletModule {

    private final String formidiumApiUrl;

    public FormidiumServletModule(final String formidiumApiUrl) {
        this.formidiumApiUrl = formidiumApiUrl;
    }

    @Override
    protected void configureServlets() {

        bind(FormidiumProxyServlet.class).asEagerSingleton();
        bind(HttpServletCORSFilter.class).asEagerSingleton();
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
        bind(HttpServletSessionIdAuthenticationFilter.class).asEagerSingleton();

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

        filter("/*").through(HttpServletGlobalSecretHeaderFilter.class);
        filter("/*").through(HttpServletSessionIdAuthenticationFilter.class);

    }

}

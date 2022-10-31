package com.namazustudios.socialengine.formidium;

import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.servlet.security.HttpServletCORSFilter;
import com.namazustudios.socialengine.servlet.security.HttpServletGlobalSecretHeaderFilter;
import com.namazustudios.socialengine.servlet.security.HttpServletSessionIdAuthenticationFilter;

import java.util.Map;

import static com.namazustudios.socialengine.servlet.security.HttpServletCORSFilter.INTERCEPT;

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

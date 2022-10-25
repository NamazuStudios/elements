package com.namazustudios.socialengine.formidium;

import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.servlet.security.HttpServletCORSFilter;
import com.namazustudios.socialengine.servlet.security.HttpServletGlobalSecretHeaderFilter;

import java.util.Map;

public class FormidiumServletModule extends ServletModule {

    private final String formidiumApiUrl;

    public FormidiumServletModule(final String formidiumApiUrl) {
        this.formidiumApiUrl = formidiumApiUrl;
    }

    @Override
    protected void configureServlets() {

        final var params = Map.of(
                "Prefix", "/",
                "ProxyTo", formidiumApiUrl
        );

        bind(FormidiumProxyServlet.class).asEagerSingleton();
        serve("/*").with(FormidiumProxyServlet.class);
        filter("/*").through(HttpServletCORSFilter.class);
        filter("/*").through(HttpServletGlobalSecretHeaderFilter.class);

    }

}

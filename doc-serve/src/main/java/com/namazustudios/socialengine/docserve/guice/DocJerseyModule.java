package com.namazustudios.socialengine.docserve.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.PrivateModule;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.docserve.DocGuiceResourceConfig;
import com.namazustudios.socialengine.rest.CORSFilter;
import com.namazustudios.socialengine.service.SessionService;
import com.namazustudios.socialengine.service.auth.DefaultSessionService;
import com.namazustudios.socialengine.servlet.security.SessionIdAuthenticationFilter;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Singleton;
import java.util.Map;

public class DocJerseyModule extends ServletModule {

    @Override
    protected void configureServlets() {

        // Setup JAX-RS resources
        bind(CORSFilter.class);

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);
        bind(SessionIdAuthenticationFilter.class).asEagerSingleton();

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
            .put("javax.ws.rs.Application", DocGuiceResourceConfig.class.getName())
            .build();

        serve("/*").with(ServletContainer.class, params);

    }
}

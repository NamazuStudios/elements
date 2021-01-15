package com.namazustudios.socialengine.cdnserve.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.cdnserve.api.DeploymentServiceProvider;
import com.namazustudios.socialengine.cdnserve.api.DeploymentService;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rest.CORSFilter;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;
import com.namazustudios.socialengine.security.UserProvider;
import com.namazustudios.socialengine.service.SessionService;
import com.namazustudios.socialengine.service.auth.DefaultSessionService;
import com.namazustudios.socialengine.servlet.security.HttpRequestAttributeAuthenticationMethod;
import com.namazustudios.socialengine.servlet.security.HttpServletBasicAuthFilter;
import com.namazustudios.socialengine.servlet.security.HttpSessionUserAuthenticationMethod;
import com.namazustudios.socialengine.servlet.security.SessionIdAuthenticationFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;

public class CdnJerseyModule extends ServletModule {

    private static final Logger logger = LoggerFactory.getLogger(CdnJerseyModule.class);

    @Override
    protected final void configureServlets() {

        // Setup JAX-RS resources
        bind(CORSFilter.class);

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);
        bind(SessionService.class).to(DefaultSessionService.class);
        bind(SessionIdAuthenticationFilter.class).asEagerSingleton();

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("javax.ws.rs.Application", CdnGuiceResourceConfig.class.getName())
            .build();

        serve("/*").with(ServletContainer.class, params);

    }

}

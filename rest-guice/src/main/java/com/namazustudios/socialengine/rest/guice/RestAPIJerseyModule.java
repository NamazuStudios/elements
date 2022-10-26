package com.namazustudios.socialengine.rest.guice;

import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.rest.*;
import com.namazustudios.socialengine.rest.support.DefaultExceptionMapper;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.servlet.security.HttpPathUtils;
import com.namazustudios.socialengine.servlet.security.HttpServletCORSFilter;
import com.namazustudios.socialengine.servlet.security.HttpServletGlobalSecretHeaderFilter;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Map;

import static com.namazustudios.socialengine.servlet.security.HttpPathUtils.normalize;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class RestAPIJerseyModule extends ServletModule {

    private final String apiRoot;

    public RestAPIJerseyModule() {
        this("");
    }

    public RestAPIJerseyModule(final String apiRoot) {
        this.apiRoot = normalize(apiRoot.isBlank() ? "/*" : apiRoot);
    }

    @Override
    protected final void configureServlets() {

        // Setup JAX-RS resources.

        bind(VersionResource.class);
        bind(SwaggerSerializers.class);
        bind(EnhancedApiListingResource.class);
        bind(MethodOverrideFilter.class);
        bind(DefaultExceptionMapper.class);
        bind(ShortLinkForwardingFilter.class);

        // Setup servlet and servlet-related features.

        bind(ServletContainer.class).asEagerSingleton();
        bind(HttpServletCORSFilter.class).asEagerSingleton();
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();

        final var params = Map.of("javax.ws.rs.Application", GuiceResourceConfig.class.getName());
        serve(apiRoot).with(ServletContainer.class, params);
        filter(apiRoot).through(HttpServletCORSFilter.class);
        filter(apiRoot).through(HttpServletGlobalSecretHeaderFilter.class);

    }
}

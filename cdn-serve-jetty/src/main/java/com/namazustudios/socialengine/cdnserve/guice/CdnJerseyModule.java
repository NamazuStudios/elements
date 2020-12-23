package com.namazustudios.socialengine.cdnserve.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.codeserve.api.deploy.DeploymentResource;
import com.namazustudios.socialengine.rest.CORSFilter;
import com.namazustudios.socialengine.rest.ShortLinkForwardingFilter;
import com.namazustudios.socialengine.rest.VersionResource;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.rt.DefaultExceptionMapper;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;

public abstract class CdnJerseyModule extends ServletModule {

    private static final Logger logger = LoggerFactory.getLogger(CdnJerseyModule.class);

    private final String apiRoot;

    public CdnJerseyModule(final String apiRoot) {
        this.apiRoot = "/" + apiRoot.replace("/.$","") + "/*";
    }

    @Override
    protected final void configureServlets() {

        // Setup JAX-RS resources

        bindSwagger();
        configureResoures();

        bind(VersionResource.class);
        bind(CORSFilter.class);
        bind(ShortLinkForwardingFilter.class);

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("javax.ws.rs.Application", CdnGuiceResourceConfig.class.getName())
                .build();

        serve(apiRoot).with(ServletContainer.class, params);

    }

    /**
     * Configures any of the resources individually.
     *
     */
    protected abstract void configureResoures();

    /**
     * Enables all resources provided by the rest-api package.
     *
     * @return this
     */
    public CdnJerseyModule enableAllResources() {
        bind(DeploymentResource.class);
        return this;
    }

    private void bindSwagger() {

        final String swaggerPackage = "io.swagger.jaxrs.listing";

        bind(SwaggerSerializers.class);
        bind(EnhancedApiListingResource.class);

    }
}

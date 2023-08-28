package dev.getelements.elements.rest.guice;

import dev.getelements.elements.guice.BaseServletModule;
import dev.getelements.elements.rest.MethodOverrideFilter;
import dev.getelements.elements.rest.ShortLinkForwardingFilter;
import dev.getelements.elements.rest.VersionResource;
import dev.getelements.elements.rest.support.DefaultExceptionMapper;
import dev.getelements.elements.rest.jersey.swagger.EnhancedApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Map;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class RestAPIJerseyModule extends BaseServletModule {

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

        final var params = Map.of("javax.ws.rs.Application", RestAPIGuiceResourceConfig.class.getName());
        serve("/*").with(ServletContainer.class, params);
        useStandardSecurityFilters();

    }

}

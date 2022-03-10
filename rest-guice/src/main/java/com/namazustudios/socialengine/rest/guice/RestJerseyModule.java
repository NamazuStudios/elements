package com.namazustudios.socialengine.rest.guice;

import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.rest.*;
import com.namazustudios.socialengine.rest.application.ApplicationConfigurationResource;
import com.namazustudios.socialengine.rest.application.ApplicationResource;
import com.namazustudios.socialengine.rest.application.PSNApplicationConfigurationResource;
import com.namazustudios.socialengine.rest.goods.ItemResource;
import com.namazustudios.socialengine.rest.security.FacebookAuthResource;
import com.namazustudios.socialengine.rest.security.UsernamePasswordResource;
import com.namazustudios.socialengine.rest.support.DefaultExceptionMapper;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.rest.user.UserResource;
import com.namazustudios.socialengine.servlet.security.HttpServletCORSFilter;
import com.namazustudios.socialengine.servlet.security.HttpServletGlobalSecretHeaderFilter;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by patricktwohig on 3/19/15.
 */
public abstract class RestJerseyModule extends ServletModule {

    private static final Logger logger = LoggerFactory.getLogger(RestJerseyModule.class);

    private final String apiRoot;

    public RestJerseyModule(final String apiRoot) {
        this.apiRoot = "/" + apiRoot.replace("/.$","") + "/*";
    }

    @Override
    protected final void configureServlets() {

        // Setup JAX-RS resources.

        bindSwagger();
        configureResoures();

        bind(VersionResource.class);
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

    /**
     * Configures any of the resources individually.
     *
     */
    protected abstract void configureResoures();

    /**
     * Enables the {@link UserResource}.
     *
     * @return this
     */
    public RestJerseyModule enableUserResource() {
        bind(UserResource.class);
        return this;
    }

    /**
     * Enables the {@link EntrantResource}.
     *
     * @return this
     */
    public RestJerseyModule enableEntrantResource() {
        bind(EntrantResource.class);
        return this;
    }

    /**
     * Enables the {@link UsernamePasswordResource}.
     *
     * @return this
     */
    public RestJerseyModule enableHttpSessionResource() {
        bind(UsernamePasswordResource.class);
        return this;
    }

    /**
     * Enables the {@link ShortLinkResource}.
     *
     * @return this
     */
    public RestJerseyModule enableShortLinkResource() {
        bind(ShortLinkResource.class);
        return this;
    }

    /**
     * Enables the {@link SocialCampaignResource}.
     *
     * @return this
     */
    public RestJerseyModule enableSocialCampaignResource() {
        bind(SocialCampaignResource.class);
        return this;
    }

    /**
     * Enables the {@link ApplicationResource}
     *
     * @return this
     */
    public RestJerseyModule enableApplicationResource() {
        bind(ApplicationResource.class);
        bind(ApplicationConfigurationResource.class);
        return this;
    }

    /**
     * Enables the {@link PSNApplicationConfigurationResource}
     *
     * @return this
     */
    public RestJerseyModule enablePSNApplicationProfileResource() {
        bind(PSNApplicationConfigurationResource.class);
        return this;
    }

    public RestJerseyModule enableItemService() {
        bind(ItemResource.class);
        return this;
    }

    /**
     * Enables all resources provided by the rest-api package.
     *
     * @return this
     */
    public RestJerseyModule enableAllResources() {
        bind(VersionResource.class);
        bind(UserResource.class);
        bind(EntrantResource.class);
        bind(UsernamePasswordResource.class);
        bind(FacebookAuthResource.class);
        bind(ShortLinkResource.class);
        bind(SocialCampaignResource.class);
        bind(ApplicationResource.class);
        bind(ApplicationConfigurationResource.class);
        bind(PSNApplicationConfigurationResource.class);
        bind(ItemResource.class);
        bind(AppleIapReceiptResource.class);
        bind(GooglePlayIapReceiptResource.class);
        bind(RewardIssuanceResource.class);
        return this;
    }

    private void bindSwagger() {

        final String swaggerPackage = "io.swagger.jaxrs.listing";

        bind(SwaggerSerializers.class);
        bind(EnhancedApiListingResource.class);

    }
}

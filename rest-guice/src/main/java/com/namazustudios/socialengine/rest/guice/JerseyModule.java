package com.namazustudios.socialengine.rest.guice;

import com.google.common.collect.ImmutableMap;
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
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;

/**
 * Created by patricktwohig on 3/19/15.
 */
public abstract class JerseyModule extends ServletModule {

    private static final Logger logger = LoggerFactory.getLogger(JerseyModule.class);

    private final String apiRoot;

    public JerseyModule(final String apiRoot) {
        this.apiRoot = "/" + apiRoot.replace("/.$","") + "/*";
    }

    @Override
    protected final void configureServlets() {

        // Setup JAX-RS resources

        bindSwagger();
        configureResoures();

        bind(VersionResource.class);
        bind(DefaultExceptionMapper.class);
        bind(CORSFilter.class);
        bind(ShortLinkForwardingFilter.class);

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                    .put("javax.ws.rs.Application", GuiceResourceConfig.class.getName())
                .build();

        serve(apiRoot).with(ServletContainer.class, params);

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
    public JerseyModule enableUserResource() {
        bind(UserResource.class);
        return this;
    }

    /**
     * Enables the {@link EntrantResource}.
     *
     * @return this
     */
    public JerseyModule enableEntrantResource() {
        bind(EntrantResource.class);
        return this;
    }

    /**
     * Enables the {@link UsernamePasswordResource}.
     *
     * @return this
     */
    public JerseyModule enableHttpSessionResource() {
        bind(UsernamePasswordResource.class);
        return this;
    }

    /**
     * Enables the {@link ShortLinkResource}.
     *
     * @return this
     */
    public JerseyModule enableShortLinkResource() {
        bind(ShortLinkResource.class);
        return this;
    }

    /**
     * Enables the {@link SocialCampaignResource}.
     *
     * @return this
     */
    public JerseyModule enableSocialCampaignResource() {
        bind(SocialCampaignResource.class);
        return this;
    }

    /**
     * Enables the {@link ApplicationResource}
     *
     * @return this
     */
    public JerseyModule enableApplicationResource() {
        bind(ApplicationResource.class);
        bind(ApplicationConfigurationResource.class);
        return this;
    }

    /**
     * Enables the {@link PSNApplicationConfigurationResource}
     *
     * @return this
     */
    public JerseyModule enablePSNApplicationProfileResource() {
        bind(PSNApplicationConfigurationResource.class);
        return this;
    }

    public JerseyModule enableItemService() {
        bind(ItemResource.class);
        return this;
    }

    /**
     * Enables all resources provided by the rest-api package.
     *
     * @return this
     */
    public JerseyModule enableAllResources() {
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
        return this;
    }

    private void bindSwagger() {

        final String swaggerPackage = "io.swagger.jaxrs.listing";

        bind(SwaggerSerializers.class);
        bind(EnhancedApiListingResource.class);

    }
}

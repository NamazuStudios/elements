package com.namazustudios.socialengine.rest.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.rest.*;
import com.namazustudios.socialengine.rest.support.DefaultExceptionMapper;
import org.glassfish.jersey.servlet.ServletContainer;
import org.reflections.Reflections;

import javax.inject.Singleton;
import java.util.Map;

/**
 * Created by patricktwohig on 3/19/15.
 */
public abstract class JerseyModule extends ServletModule {

    private final String apiRoot;

    public JerseyModule(final String apiRoot) {
        this.apiRoot = apiRoot.replace("/.$","") + "/*";
    }

    @Override
    protected final void configureServlets() {

        // Setup JAX-RS resources

        configureResoures();
        bind(DefaultExceptionMapper.class);

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
     * Enables the {@link SessionResource}.
     *
     * @return this
     */
    public JerseyModule enableSessionResource() {
        bind(SessionResource.class);
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
        bind(ApplicationProfileResource.class);
        return this;
    }

    /**
     * Enables the {@link PSNApplicationProfileResource}
     *
     * @return this
     */
    public JerseyModule enablePSNApplicationProfileResource() {
        bind(PSNApplicationProfileResource.class);
        return this;
    }

    /**
     * Enables all resources provided by the rest-api package.
     *
     * @return this
     */
    public JerseyModule enableAllResources() {
        bind(UserResource.class);
        bind(EntrantResource.class);
        bind(SessionResource.class);
        bind(ShortLinkResource.class);
        bind(SocialCampaignResource.class);
        bind(ApplicationResource.class);
        bind(ApplicationProfileResource.class);
        bind(PSNApplicationProfileResource.class);
        return this;
    }

}

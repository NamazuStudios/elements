package com.namazustudios.socialengine.rest.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.rest.*;
import com.namazustudios.socialengine.rest.support.DefaultExceptionMapper;
import org.glassfish.jersey.servlet.ServletContainer;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;

import static java.lang.reflect.Modifier.ABSTRACT;

/**
 * Created by patricktwohig on 3/19/15.
 */
public abstract class JerseyModule extends ServletModule {

    private static final Logger LOG = LoggerFactory.getLogger(JerseyModule.class);

    private final String apiRoot;

    public JerseyModule(final String apiRoot) {
        this.apiRoot = "/" + apiRoot.replace("/.$","") + "/*";
    }

    @Override
    protected final void configureServlets() {

        // Setup JAX-RS resources

        bindSwagger();
        configureResoures();

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
        bind(ApplicationConfigurationResource.class);
        bind(PSNApplicationConfigurationResource.class);
        return this;
    }

    private void bindSwagger() {

        final String swaggerPackage = "io.swagger.jaxrs.listing";


        final Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(swaggerPackage)
                        .filterInputsBy(new FilterBuilder().includePackage(swaggerPackage))
                        .setScanners(new SubTypesScanner(false)));

        LOG.info("Scanning package io.swagger.jaxrs.listing for inclusion into JAX-RS");

        for (final String type : reflections.getAllTypes()) {

            final Class<?> cls;

            try {

                cls = Class.forName(type);

                if ((cls.getModifiers() & ABSTRACT) == 0) {
                    bind(cls);
                }

            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }

        }

    }
}

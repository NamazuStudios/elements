package com.namazustudios.promotion.rest.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.namazustudios.promotion.rest.BasicEntrantResource;
import com.namazustudios.promotion.rest.SocialCampaignResource;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Singleton;
import java.util.Map;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class JerseyModule extends ServletModule {

    @Override
    protected void configureServlets() {

        // Setup JAX-RS resources

        bind(BasicEntrantResource.class);
        bind(SocialCampaignResource.class);

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("javax.ws.rs.Application", GuiceResourceConfig.class.getName())
                .build();

        serve("/*").with(ServletContainer.class, params);

    }

}

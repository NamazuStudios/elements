package com.namazustudios.promotion.rest.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.promotion.rest.EntrantResource;
import com.namazustudios.promotion.rest.SocialCampaignResource;
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

        bind(EntrantResource.class);
        bind(SocialCampaignResource.class);

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("javax.ws.rs.Application", GuiceResourceConfig.class.getName())
                .build();

        serve("/*").with(ServletContainer.class, params);

    }

}

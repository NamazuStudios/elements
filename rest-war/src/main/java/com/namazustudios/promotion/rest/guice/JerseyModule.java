package com.namazustudios.promotion.rest.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.promotion.rest.EntrantResource;
import com.namazustudios.promotion.rest.SessionResource;
import com.namazustudios.promotion.rest.SocialCampaignResource;
import com.namazustudios.promotion.rest.UserResource;
import com.namazustudios.promotion.rest.support.DefaultExceptionMapper;
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

        bind(UserResource.class);
        bind(EntrantResource.class);
        bind(SessionResource.class);
        bind(SocialCampaignResource.class);
        bind(DefaultExceptionMapper.class);

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("javax.ws.rs.Application", GuiceResourceConfig.class.getName())
                .build();

        serve("/*").with(ServletContainer.class, params);

    }

}

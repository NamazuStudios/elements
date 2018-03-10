package com.namazustudios.socialengine.server.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.rest.support.DefaultExceptionMapper;
import com.namazustudios.socialengine.server.UiConfigResource;
import com.namazustudios.socialengine.server.VersionResource;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Singleton;
import java.util.Map;

/**
 * The UI has a smaller API
 *
 * Created by patricktwohig on 5/11/17.
 */
public class UiGuiceServletModule extends ServletModule {

    public static final String API_ROOT = "/ui/*";

    @Override
    protected void configureServlets() {

        // Setup JAX-RS resources

        bind(VersionResource.class);
        bind(UiConfigResource.class);
        bind(DefaultExceptionMapper.class);

        // Setup servlets

        bind(ServletContainer.class).in(Singleton.class);

        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("javax.ws.rs.Application", UiGuiceResourceConfig.class.getName())
            .build();

        serve(API_ROOT).with(ServletContainer.class, params);

    }

}

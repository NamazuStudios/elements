package com.namazustudios.socialengine.rest.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.ShortLinkForwardingFilter;

import javax.inject.Singleton;

/**
 * Created by patricktwohig on 6/12/15.
 */
public class ShortLinkFilterModule extends ServletModule {

    private final String apiRoot;

    public ShortLinkFilterModule(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    protected void configureServlets() {
        bind(ShortLinkForwardingFilter.class).in(Singleton.class);
        filter("/*").through(ShortLinkForwardingFilter.class, new ImmutableMap.Builder<String, String>()
                .put(ShortLinkForwardingFilter.API_ROOT, apiRoot)
            .build());
    }

}

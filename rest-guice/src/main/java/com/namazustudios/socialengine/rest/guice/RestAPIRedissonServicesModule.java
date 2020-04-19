package com.namazustudios.socialengine.rest.guice;

import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.service.guice.RedissonServicesModule;

public class RestAPIRedissonServicesModule extends RedissonServicesModule {

    public RestAPIRedissonServicesModule() {
        super(ServletScopes.REQUEST);
    }

}

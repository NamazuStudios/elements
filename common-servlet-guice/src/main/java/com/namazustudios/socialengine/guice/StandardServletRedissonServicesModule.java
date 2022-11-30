package com.namazustudios.socialengine.guice;

import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.service.guice.RedissonServicesModule;

public class StandardServletRedissonServicesModule extends RedissonServicesModule {

    public StandardServletRedissonServicesModule() {
        super(ServletScopes.REQUEST);
    }

}

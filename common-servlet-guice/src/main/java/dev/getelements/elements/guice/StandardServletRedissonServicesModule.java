package dev.getelements.elements.guice;

import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.service.guice.RedissonServicesModule;

public class StandardServletRedissonServicesModule extends RedissonServicesModule {

    public StandardServletRedissonServicesModule() {
        super(ServletScopes.REQUEST);
    }

}

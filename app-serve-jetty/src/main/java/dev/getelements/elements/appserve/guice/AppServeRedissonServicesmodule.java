package dev.getelements.elements.appserve.guice;

import dev.getelements.elements.service.guice.RedissonServicesModule;
import dev.getelements.elements.rt.guice.RequestScope;

public class AppServeRedissonServicesmodule extends RedissonServicesModule {

    public AppServeRedissonServicesmodule() {
        super(RequestScope.getInstance());
    }

}

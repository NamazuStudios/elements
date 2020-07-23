package com.namazustudios.socialengine.appserve.guice;

import com.namazustudios.socialengine.service.guice.RedissonServicesModule;
import com.namazustudios.socialengine.rt.guice.RequestScope;

public class AppServeRedissonServicesmodule extends RedissonServicesModule {

    public AppServeRedissonServicesmodule() {
        super(RequestScope.getInstance());
    }

}

package com.namazustudios.socialengine.rpc.guice;

import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.service.guice.RedissonServicesModule;

public class RpcApiRedissonServicesModule extends RedissonServicesModule {

    public RpcApiRedissonServicesModule() {
        super(RequestScope.getInstance());
    }

}

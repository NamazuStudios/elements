package com.namazustudios.socialengine.rpc.guice;

import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.rt.servlet.HttpServletAttributesProvider;
import com.namazustudios.socialengine.service.guice.ServicesModule;

public class RpcApiServicesModule extends ServicesModule {

    public RpcApiServicesModule() {
        super(RequestScope.getInstance(), HttpServletAttributesProvider.class);
    }

}


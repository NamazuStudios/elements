package com.namazustudios.socialengine.appserve.guice;

import com.namazustudios.socialengine.rt.RequestAttributesProvider;
import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.service.guice.ServicesModule;

public class AppServeServicesModule extends ServicesModule {

    public AppServeServicesModule() {
        super(RequestScope.getInstance(), RequestAttributesProvider.class);
    }

}

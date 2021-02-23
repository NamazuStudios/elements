package com.namazustudios.socialengine.appnode.guice;

import com.namazustudios.socialengine.rt.ResourceAttributesProvider;
import com.namazustudios.socialengine.rt.guice.ResourceScope;
import com.namazustudios.socialengine.service.guice.ServicesModule;

public class AppNodeServicesModule extends ServicesModule {

    public AppNodeServicesModule() {
        super(ResourceScope.getInstance(), ResourceAttributesProvider.class);
    }

    @Override
    protected void configure() {
        super.configure();
        ResourceScope.bind(binder());
    }

}

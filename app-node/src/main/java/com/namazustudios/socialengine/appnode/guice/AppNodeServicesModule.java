package com.namazustudios.socialengine.appnode.guice;

import com.namazustudios.socialengine.rt.CurrentResource;
import com.namazustudios.socialengine.rt.Resource;
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

        expose(Resource.class);
        bind(Resource.class).toProvider(() -> CurrentResource.getInstance().getCurrent());

    }

}

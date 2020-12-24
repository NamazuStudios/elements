package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.appnode.security.ResourceOptionalSessionProvider;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.rt.CurrentResource;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceAttributesProvider;
import com.namazustudios.socialengine.rt.guice.ResourceScope;
import com.namazustudios.socialengine.security.SessionProvider;
import com.namazustudios.socialengine.service.guice.ServicesModule;

import java.util.Optional;

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

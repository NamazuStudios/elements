package com.namazustudios.socialengine.docserve.guice;

import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.rt.servlet.HttpServletAttributesProvider;
import com.namazustudios.socialengine.service.guice.ServicesModule;

public class DocServeServicesModule extends ServicesModule {

    public DocServeServicesModule() {
        super(ServletScopes.REQUEST, HttpServletAttributesProvider.class);
    }

}

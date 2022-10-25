package com.namazustudios.socialengine.guice;

import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.rt.servlet.HttpServletAttributesProvider;
import com.namazustudios.socialengine.service.guice.ServicesModule;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class StandardServletServicesModule extends ServicesModule {

    public StandardServletServicesModule() {
        super(ServletScopes.REQUEST, HttpServletAttributesProvider.class);
    }

}

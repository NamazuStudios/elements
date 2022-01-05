package com.namazustudios.socialengine.rest.guice;

import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.service.guice.ServicesModule;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class RestAPIServicesModule extends ServicesModule {

    public RestAPIServicesModule() {
        super(ServletScopes.REQUEST, AttributesProvider.class);
    }

}

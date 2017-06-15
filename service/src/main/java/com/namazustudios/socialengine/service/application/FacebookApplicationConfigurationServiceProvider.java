package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.service.FacebookApplicationConfigurationService;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 6/14/17.
 */
public class FacebookApplicationConfigurationServiceProvider implements Provider<FacebookApplicationConfigurationService> {

    @Override
    public FacebookApplicationConfigurationService get() {
        // TODO Implement proper service.
        return Services.forbidden(FacebookApplicationConfigurationService.class);
    }

}

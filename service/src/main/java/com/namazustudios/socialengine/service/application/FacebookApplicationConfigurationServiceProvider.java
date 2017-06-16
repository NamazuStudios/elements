package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.FacebookApplicationConfigurationService;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 6/14/17.
 */
public class FacebookApplicationConfigurationServiceProvider implements Provider<FacebookApplicationConfigurationService> {

    @Inject
    private User user;

    @Inject
    private Provider<SuperUserFacebookApplicationConfigurationService> superUserFacebookApplicationConfigurationServiceProvider;

    @Override
    public FacebookApplicationConfigurationService get() {
        switch (user.getLevel()) {
            case SUPERUSER:
                return superUserFacebookApplicationConfigurationServiceProvider.get();
            default:
                return Services.forbidden(FacebookApplicationConfigurationService.class);
        }
    }

}

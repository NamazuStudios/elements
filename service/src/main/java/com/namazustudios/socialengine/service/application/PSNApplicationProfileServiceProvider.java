package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.PSNApplicationProfileService;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/24/17.
 */
public class PSNApplicationProfileServiceProvider implements Provider<PSNApplicationProfileService> {

    @Inject
    private User user;

    @Inject
    private Provider<PSNApplicationProfileService> superUserPSNApplicationProfileServiceProvider;

    @Override
    public PSNApplicationProfileService get() {
        switch (user.getLevel()) {
            case SUPERUSER:
                return superUserPSNApplicationProfileServiceProvider.get();
            default:
                return Services.forbidden(PSNApplicationProfileService.class);

        }
    }

}

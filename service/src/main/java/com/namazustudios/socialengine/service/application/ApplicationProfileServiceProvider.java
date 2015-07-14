package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.ApplicationProfileService;
import com.namazustudios.socialengine.service.ApplicationService;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Selects the appropriate {@link ApplicationProfileService} based on  the {@link User#getLevel()} property.
 *
 * Created by patricktwohig on 7/13/15.
 */
public class ApplicationProfileServiceProvider implements Provider<ApplicationProfileService> {

    @Inject
    private User user;

    @Inject
    private Provider<SuperUserApplicationProfileService> superUserApplicationProfileServiceProvider;

    @Override
    public ApplicationProfileService get() {
        switch (user.getLevel()) {
            case SUPERUSER:
                return superUserApplicationProfileServiceProvider.get();
            default:
                return Services.forbidden(ApplicationProfileService.class);

        }
    }

}

package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.ApplicationConfigurationService;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Selects the appropriate {@link ApplicationConfigurationService} based on  the {@link User#getLevel()} property.
 *
 * Created by patricktwohig on 7/13/15.
 */
public class ApplicationConfigurationServiceProvider implements Provider<ApplicationConfigurationService> {

    @Inject
    private User user;

    @Inject
    private Provider<SuperUserApplicationConfigurationService> superUserApplicationProfileServiceProvider;

    @Override
    public ApplicationConfigurationService get() {
        switch (user.getLevel()) {
            case SUPERUSER:
                return superUserApplicationProfileServiceProvider.get();
            default:
                return Services.forbidden(ApplicationConfigurationService.class);

        }
    }

}

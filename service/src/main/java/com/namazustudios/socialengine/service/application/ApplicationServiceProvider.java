package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.ApplicationService;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 7/10/15.
 */
public class ApplicationServiceProvider implements Provider<ApplicationService> {

    @Inject
    private User user;

    @Inject
    private Provider<SuperUserApplicationService> superUserApplicationServiceProvider;

    @Override
    public ApplicationService get() {
        switch (user.getLevel()) {
        case SUPERUSER:
            return superUserApplicationServiceProvider.get();
        default:
            return Services.forbidden(ApplicationService.class);

        }
    }

}

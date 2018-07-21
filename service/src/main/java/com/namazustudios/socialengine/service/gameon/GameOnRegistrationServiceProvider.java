package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.GameOnRegistrationService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class GameOnRegistrationServiceProvider implements Provider<GameOnRegistrationService> {

    private Provider<User> userProvider;

    private Provider<UserGameOnRegistrationService> userGameOnRegistrationServiceProvider;

    @Override
    public GameOnRegistrationService get() {
        switch (getUserProvider().get().getLevel()) {

            // Users and super users may register.  However.  Super users may not even delete others credentials at
            // the moment.  This should probably be addressed in the future allowing super users to manually remove
            // or manipulate various GameOn registrations.

            case USER:
            case SUPERUSER:     return getUserGameOnRegistrationServiceProvider().get();

            // Default access level is forbidden from accessing anything.
            default:            return forbidden(GameOnRegistrationService.class);

        }
    }

    public Provider<User> getUserProvider() {
        return userProvider;
    }

    @Inject
    public void setUserProvider(Provider<User> userProvider) {
        this.userProvider = userProvider;
    }

    public Provider<UserGameOnRegistrationService> getUserGameOnRegistrationServiceProvider() {
        return userGameOnRegistrationServiceProvider;
    }

    @Inject
    public void setUserGameOnRegistrationServiceProvider(Provider<UserGameOnRegistrationService> userGameOnRegistrationServiceProvider) {
        this.userGameOnRegistrationServiceProvider = userGameOnRegistrationServiceProvider;
    }

}

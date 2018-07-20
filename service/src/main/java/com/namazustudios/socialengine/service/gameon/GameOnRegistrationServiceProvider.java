package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.GameOnRegistrationService;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.unimplemented;

public class GameOnRegistrationServiceProvider implements Provider<GameOnRegistrationService> {

    private Provider<User> userProvider;

    @Override
    public GameOnRegistrationService get() {
        switch (getUserProvider().get().getLevel()) {
            // TODO Fill in User Levels
            default: return unimplemented(GameOnRegistrationService.class);
        }
    }

    public Provider<User> getUserProvider() {
        return userProvider;
    }

    @Inject
    public void setUserProvider(Provider<User> userProvider) {
        this.userProvider = userProvider;
    }

}

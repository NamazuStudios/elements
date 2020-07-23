package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.GameOnMatchService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class GameOnMatchServiceProvider implements Provider<GameOnMatchService> {

    private User user;

    private Provider<UserGameOnMatchService> userGameOnMatchServiceProvider;

    @Override
    public GameOnMatchService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:      return getUserGameOnMatchServiceProvider().get();
            default:        return forbidden(GameOnMatchService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserGameOnMatchService> getUserGameOnMatchServiceProvider() {
        return userGameOnMatchServiceProvider;
    }

    @Inject
    public void setUserGameOnMatchServiceProvider(Provider<UserGameOnMatchService> userGameOnMatchServiceProvider) {
        this.userGameOnMatchServiceProvider = userGameOnMatchServiceProvider;
    }

}

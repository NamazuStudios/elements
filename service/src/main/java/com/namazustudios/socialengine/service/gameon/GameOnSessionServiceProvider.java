package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.GameOnSessionService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class GameOnSessionServiceProvider implements Provider<GameOnSessionService> {

    private User user;

    private Provider<UserGameOnSessionService> userGameOnSessionServiceProvider;

    @Override
    public GameOnSessionService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:      return getUserGameOnSessionServiceProvider().get();
            default:        return forbidden(GameOnSessionService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserGameOnSessionService> getUserGameOnSessionServiceProvider() {
        return userGameOnSessionServiceProvider;
    }

    @Inject
    public void setUserGameOnSessionServiceProvider(Provider<UserGameOnSessionService> userGameOnSessionServiceProvider) {
        this.userGameOnSessionServiceProvider = userGameOnSessionServiceProvider;
    }

}

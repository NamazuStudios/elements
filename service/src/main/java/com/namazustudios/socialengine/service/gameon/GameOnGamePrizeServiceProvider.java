package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.GameOnGamePrizeService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class GameOnGamePrizeServiceProvider implements Provider<GameOnGamePrizeService> {

    private User user;

    private Provider<UserGameOnGamePrizeService> userGameOnGamePrizeServiceProvider;

    @Override
    public GameOnGamePrizeService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:    return getUserGameOnGamePrizeServiceProvider().get();
            default:      return forbidden(GameOnGamePrizeService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserGameOnGamePrizeService> getUserGameOnGamePrizeServiceProvider() {
        return userGameOnGamePrizeServiceProvider;
    }

    @Inject
    public void setUserGameOnGamePrizeServiceProvider(Provider<UserGameOnGamePrizeService> userGameOnGamePrizeServiceProvider) {
        this.userGameOnGamePrizeServiceProvider = userGameOnGamePrizeServiceProvider;
    }

}

package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.GameOnPlayerTournamentService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class GameOnPlayerTournamentServiceProvider implements Provider<GameOnPlayerTournamentService> {

    private User user;

    private Provider<UserGameOnPlayerTournamentService> userGameOnPlayerTournamentServiceProvider;

    @Override
    public GameOnPlayerTournamentService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:      return getUserGameOnPlayerTournamentServiceProvider().get();
            default:        return forbidden(GameOnPlayerTournamentService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserGameOnPlayerTournamentService> getUserGameOnPlayerTournamentServiceProvider() {
        return userGameOnPlayerTournamentServiceProvider;
    }

    @Inject
    public void setUserGameOnPlayerTournamentServiceProvider(Provider<UserGameOnPlayerTournamentService> userGameOnPlayerTournamentServiceProvider) {
        this.userGameOnPlayerTournamentServiceProvider = userGameOnPlayerTournamentServiceProvider;
    }

}

package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.GameOnTournamentService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class GameOnTournamentServiceProvider implements Provider<GameOnTournamentService> {

    private User user;

    private Provider<UserGameOnTournamentService> userGameOnTournamentServiceProvider;

    @Override
    public GameOnTournamentService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:      return getUserGameOnTournamentServiceProvider().get();
            default:        return forbidden(GameOnTournamentService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserGameOnTournamentService> getUserGameOnTournamentServiceProvider() {
        return userGameOnTournamentServiceProvider;
    }

    @Inject
    public void setUserGameOnTournamentServiceProvider(Provider<UserGameOnTournamentService> userGameOnTournamentServiceProvider) {
        this.userGameOnTournamentServiceProvider = userGameOnTournamentServiceProvider;
    }

}

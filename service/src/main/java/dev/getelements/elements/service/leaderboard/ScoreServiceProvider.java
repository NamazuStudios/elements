package dev.getelements.elements.service.leaderboard;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.leaderboard.ScoreService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class ScoreServiceProvider implements Provider<ScoreService> {

    private User user;

    private Provider<UserScoreService> userScoreServiceProvider;

    @Override
    public ScoreService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:     return getUserScoreServiceProvider().get();
            default:            return forbidden(ScoreService.class);
        }
    }


    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserScoreService> getUserScoreServiceProvider() {
        return userScoreServiceProvider;
    }

    @Inject
    public void setUserScoreServiceProvider(Provider<UserScoreService> userScoreServiceProvider) {
        this.userScoreServiceProvider = userScoreServiceProvider;
    }

}

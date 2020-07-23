package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.ScoreService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

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

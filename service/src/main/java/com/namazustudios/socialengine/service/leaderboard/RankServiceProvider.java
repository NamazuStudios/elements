package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.RankService;

import javax.inject.Inject;
import javax.inject.Provider;

public class RankServiceProvider implements Provider<RankService> {

    private User user;

    private Provider<UserRankService> userRankServiceProvider;

    private Provider<AnonRankService> anonRankServiceProvider;

    @Override
    public RankService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:      return getUserRankServiceProvider().get();
            default:        return getAnonRankServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserRankService> getUserRankServiceProvider() {
        return userRankServiceProvider;
    }

    @Inject
    public void setUserRankServiceProvider(Provider<UserRankService> userRankServiceProvider) {
        this.userRankServiceProvider = userRankServiceProvider;
    }

    public Provider<AnonRankService> getAnonRankServiceProvider() {
        return anonRankServiceProvider;
    }

    @Inject
    public void setAnonRankServiceProvider(Provider<AnonRankService> anonRankServiceProvider) {
        this.anonRankServiceProvider = anonRankServiceProvider;
    }

}

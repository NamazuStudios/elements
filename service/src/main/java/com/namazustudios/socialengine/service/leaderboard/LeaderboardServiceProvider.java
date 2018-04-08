package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.LeaderboardService;

import javax.inject.Inject;
import javax.inject.Provider;

public class LeaderboardServiceProvider implements Provider<LeaderboardService> {

    private User user;

    private Provider<AnonLeaderboardService> anonLeaderboardServiceProvider;

    private Provider<SuperUserLeaderboardService> superUserLeaderboardProviderProvider;

    @Override
    public LeaderboardService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:     return getSuperUserLeaderboardProviderProvider().get();
            default:            return getAnonLeaderboardServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonLeaderboardService> getAnonLeaderboardServiceProvider() {
        return anonLeaderboardServiceProvider;
    }

    @Inject
    public void setAnonLeaderboardServiceProvider(Provider<AnonLeaderboardService> anonLeaderboardServiceProvider) {
        this.anonLeaderboardServiceProvider = anonLeaderboardServiceProvider;
    }

    public Provider<SuperUserLeaderboardService> getSuperUserLeaderboardProviderProvider() {
        return superUserLeaderboardProviderProvider;
    }

    @Inject
    public void setSuperUserLeaderboardProviderProvider(Provider<SuperUserLeaderboardService> superUserLeaderboardProviderProvider) {
        this.superUserLeaderboardProviderProvider = superUserLeaderboardProviderProvider;
    }

}

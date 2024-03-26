package dev.getelements.elements.service.leaderboard;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.RankService;

import javax.inject.Inject;
import javax.inject.Provider;

public class RankServiceProvider implements Provider<RankService> {

    private User user;

    private Provider<SuperUserRankService> superUserRankServiceProvider;

    private Provider<UserRankService> userRankServiceProvider;

    private Provider<AnonRankService> anonRankServiceProvider;

    @Override
    public RankService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER: return getSuperUserRankServiceProvider().get();
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

    public Provider<SuperUserRankService> getSuperUserRankServiceProvider() {
        return superUserRankServiceProvider;
    }

    @Inject
    public void setSuperUserRankServiceProvider(Provider<SuperUserRankService> superUserRankServiceProvider) {
        this.superUserRankServiceProvider = superUserRankServiceProvider;
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

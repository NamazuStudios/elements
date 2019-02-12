package com.namazustudios.socialengine.service.reward;

import com.namazustudios.socialengine.model.User;

import javax.inject.Inject;
import javax.inject.Provider;

public class RewardServiceProvider implements Provider<RewardService> {

    private User user;

    private Provider<AnonRewardService> anonRewardServiceProvider;

    private Provider<SuperUserRewardService> superUserRewardServiceProvider;

    @Override
    public RewardService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserRewardServiceProvider().get();
            default:
                return getAnonRewardServiceProvider().get();
       }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonRewardService> getAnonRewardServiceProvider() {
        return anonRewardServiceProvider;
    }

    @Inject
    public void setAnonRewardServiceProvider(Provider<AnonRewardService> anonRewardServiceProvider) {
        this.anonRewardServiceProvider = anonRewardServiceProvider;
    }

    public Provider<SuperUserRewardService> getSuperUserRewardServiceProvider() {
        return superUserRewardServiceProvider;
    }

    @Inject
    public void setSuperUserRewardServiceProvider(Provider<SuperUserRewardService> superUserRewardServiceProvider) {
        this.superUserRewardServiceProvider = superUserRewardServiceProvider;
    }
}

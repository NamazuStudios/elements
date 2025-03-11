package dev.getelements.elements.service.rewardissuance;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.rewardissuance.RewardIssuanceService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class RewardIssuanceServiceProvider implements Provider<RewardIssuanceService> {

    private User user;

    private Provider<UserRewardIssuanceService> userRewardIssuanceServiceProvider;

    @Override
    public RewardIssuanceService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:
                return getUserRewardIssuanceServiceProvider().get();
            default:
                throw new ForbiddenException("User must be logged in.");
       }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserRewardIssuanceService> getUserRewardIssuanceServiceProvider() {
        return userRewardIssuanceServiceProvider;
    }

    @Inject
    public void setUserRewardIssuanceServiceProvider(Provider<UserRewardIssuanceService> userRewardIssuanceServiceProvider) {
        this.userRewardIssuanceServiceProvider = userRewardIssuanceServiceProvider;
    }
}

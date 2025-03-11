package dev.getelements.elements.service.follower;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.follower.FollowerService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

/**
 * Created by GarrettMcSpadden on 12/7/2020.
 */
public class FollowerServiceProvider implements Provider<FollowerService> {

    private User user;

    private Provider<UserFollowerService> userFollowerServiceProvider;

    private Provider<SuperUserFollowerService> superUserFollowerServiceProvider;

    @Override
    public FollowerService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserFollowerServiceProvider().get();
            case USER:
                return getUserFollowerServiceProvider().get();
            default:
                return forbidden(FollowerService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserFollowerService> getUserFollowerServiceProvider() {
        return userFollowerServiceProvider;
    }

    @Inject
    public void setUserFollowerServiceProvider(Provider<UserFollowerService> userFollowerServiceProvider) {
        this.userFollowerServiceProvider = userFollowerServiceProvider;
    }

    public Provider<SuperUserFollowerService> getSuperUserFollowerServiceProvider() {
        return superUserFollowerServiceProvider;
    }

    @Inject
    public void setSuperUserFollowerServiceProvider(Provider<SuperUserFollowerService> superUserFollowerServiceProvider) {
        this.superUserFollowerServiceProvider = superUserFollowerServiceProvider;
    }

}

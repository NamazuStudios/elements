package dev.getelements.elements.service.friend;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.friend.FriendService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class FriendServiceProvider implements Provider<FriendService> {

    private User user;

    private Provider<UserFriendService> userFriendServiceProvider;

    @Override
    public FriendService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER: return getUserFriendServiceProvider().get();
            default:        return forbidden(FriendService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserFriendService> getUserFriendServiceProvider() {
        return userFriendServiceProvider;
    }

    @Inject
    public void setUserFriendServiceProvider(Provider<UserFriendService> userFriendServiceProvider) {
        this.userFriendServiceProvider = userFriendServiceProvider;
    }

}

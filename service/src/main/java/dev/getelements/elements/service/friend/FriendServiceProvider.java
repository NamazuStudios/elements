package dev.getelements.elements.service.friend;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.FriendService;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.service.Services.forbidden;

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

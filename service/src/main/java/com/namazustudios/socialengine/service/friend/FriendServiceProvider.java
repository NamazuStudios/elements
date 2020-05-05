package com.namazustudios.socialengine.service.friend;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.FriendService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

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

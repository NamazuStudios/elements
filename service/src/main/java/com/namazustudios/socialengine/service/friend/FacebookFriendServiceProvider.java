package com.namazustudios.socialengine.service.friend;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.FacebookFriendService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class FacebookFriendServiceProvider implements Provider<FacebookFriendService> {

    private User user;

    private Provider<FacebookFriendService> facebookFriendServiceProvider;

    @Override
    public FacebookFriendService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:    return getFacebookFriendServiceProvider().get();
            default:           return forbidden(FacebookFriendService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<FacebookFriendService> getFacebookFriendServiceProvider() {
        return facebookFriendServiceProvider;
    }

    @Inject
    public void setFacebookFriendServiceProvider(Provider<FacebookFriendService> facebookFriendServiceProvider) {
        this.facebookFriendServiceProvider = facebookFriendServiceProvider;
    }

}

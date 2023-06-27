package dev.getelements.elements.service.friend;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.FacebookFriendService;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.service.Services.forbidden;

public class FacebookFriendServiceProvider implements Provider<FacebookFriendService> {

    private User user;

    private Provider<UserFacebookFriendService> userFacebookFriendServiceProvider;

    @Override
    public FacebookFriendService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:    return getUserFacebookFriendServiceProvider().get();
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

    public Provider<UserFacebookFriendService> getUserFacebookFriendServiceProvider() {
        return userFacebookFriendServiceProvider;
    }

    @Inject
    public void setUserFacebookFriendServiceProvider(Provider<UserFacebookFriendService> userFacebookFriendServiceProvider) {
        this.userFacebookFriendServiceProvider = userFacebookFriendServiceProvider;
    }

}

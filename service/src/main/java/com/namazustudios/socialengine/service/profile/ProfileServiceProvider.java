package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.ProfileService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

/**
 * Created by patricktwohig on 6/27/17.
 */
public class ProfileServiceProvider implements Provider<ProfileService> {

    private User user;

    private Provider<UserProfileService> userProfileServiceProvider;

    private Provider<SuperUserProfileService> superUserProfileServiceProvider;

    @Override
    public ProfileService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserProfileServiceProvider().get();
            case USER:
                return getUserProfileServiceProvider().get();
            default:
                return forbidden(ProfileService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserProfileService> getUserProfileServiceProvider() {
        return userProfileServiceProvider;
    }

    @Inject
    public void setUserProfileServiceProvider(Provider<UserProfileService> userProfileServiceProvider) {
        this.userProfileServiceProvider = userProfileServiceProvider;
    }

    public Provider<SuperUserProfileService> getSuperUserProfileServiceProvider() {
        return superUserProfileServiceProvider;
    }

    @Inject
    public void setSuperUserProfileServiceProvider(Provider<SuperUserProfileService> superUserProfileServiceProvider) {
        this.superUserProfileServiceProvider = superUserProfileServiceProvider;
    }

}

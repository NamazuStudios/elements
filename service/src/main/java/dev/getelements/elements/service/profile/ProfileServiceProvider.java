package dev.getelements.elements.service.profile;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.profile.ProfileService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

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

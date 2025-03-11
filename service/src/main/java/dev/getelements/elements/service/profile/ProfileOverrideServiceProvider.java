package dev.getelements.elements.service.profile;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.profile.ProfileOverrideService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ProfileOverrideServiceProvider implements Provider<ProfileOverrideService> {

    private User user;

    private Provider<AnonProfileOverrideService> anonProfileOverrideServiceProvider;

    private Provider<UserProfileOverrideService> userProfileOverrideServiceProvider;

    private Provider<SuperUserProfileOverrideService> superUserProfileOverrideServiceProvider;

    @Override
    public ProfileOverrideService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserProfileOverrideServiceProvider().get();
            case USER:
                return getUserProfileOverrideServiceProvider().get();
            default:
                return getAnonProfileOverrideServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonProfileOverrideService> getAnonProfileOverrideServiceProvider() {
        return anonProfileOverrideServiceProvider;
    }

    @Inject
    public void setAnonProfileOverrideServiceProvider(Provider<AnonProfileOverrideService> anonProfileOverrideServiceProvider) {
        this.anonProfileOverrideServiceProvider = anonProfileOverrideServiceProvider;
    }

    public Provider<UserProfileOverrideService> getUserProfileOverrideServiceProvider() {
        return userProfileOverrideServiceProvider;
    }

    @Inject
    public void setUserProfileOverrideServiceProvider(Provider<UserProfileOverrideService> userProfileOverrideServiceProvider) {
        this.userProfileOverrideServiceProvider = userProfileOverrideServiceProvider;
    }

    public Provider<SuperUserProfileOverrideService> getSuperUserProfileOverrideServiceProvider() {
        return superUserProfileOverrideServiceProvider;
    }

    @Inject
    public void setSuperUserProfileOverrideServiceProvider(Provider<SuperUserProfileOverrideService> superUserProfileOverrideServiceProvider) {
        this.superUserProfileOverrideServiceProvider = superUserProfileOverrideServiceProvider;
    }

}

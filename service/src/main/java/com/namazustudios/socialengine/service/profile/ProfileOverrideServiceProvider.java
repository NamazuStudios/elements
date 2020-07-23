package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.ProfileOverrideService;

import javax.inject.Inject;
import javax.inject.Provider;

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

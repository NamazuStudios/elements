package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.FacebookApplicationConfigurationService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 6/14/17.
 */
public class FacebookApplicationConfigurationServiceProvider implements Provider<FacebookApplicationConfigurationService> {

    private User user;

    private Provider<AnonFacebookApplicationConfigurationService> anonFacebookApplicationConfigurationServiceProvider;

    private Provider<SuperUserFacebookApplicationConfigurationService> superUserFacebookApplicationConfigurationServiceProvider;

    @Override
    public FacebookApplicationConfigurationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserFacebookApplicationConfigurationServiceProvider().get();
            default:
                return getAnonFacebookApplicationConfigurationServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonFacebookApplicationConfigurationService> getAnonFacebookApplicationConfigurationServiceProvider() {
        return anonFacebookApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setAnonFacebookApplicationConfigurationServiceProvider(Provider<AnonFacebookApplicationConfigurationService> anonFacebookApplicationConfigurationServiceProvider) {
        this.anonFacebookApplicationConfigurationServiceProvider = anonFacebookApplicationConfigurationServiceProvider;
    }

    public Provider<SuperUserFacebookApplicationConfigurationService> getSuperUserFacebookApplicationConfigurationServiceProvider() {
        return superUserFacebookApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setSuperUserFacebookApplicationConfigurationServiceProvider(Provider<SuperUserFacebookApplicationConfigurationService> superUserFacebookApplicationConfigurationServiceProvider) {
        this.superUserFacebookApplicationConfigurationServiceProvider = superUserFacebookApplicationConfigurationServiceProvider;
    }

}

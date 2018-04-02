package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.FirebaseApplicationConfigurationService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class FirebaseApplicationConfigurationServiceProvider implements Provider<FirebaseApplicationConfigurationService> {

    private User user;

    private Provider<SuperUserFirebaseApplicationConfigurationService> superUserFirebaseApplicationConfigurationServiceProvider;

    @Override
    public FirebaseApplicationConfigurationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER: return getSuperUserFirebaseApplicationConfigurationServiceProvider().get();
            default:        return forbidden(FirebaseApplicationConfigurationService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserFirebaseApplicationConfigurationService> getSuperUserFirebaseApplicationConfigurationServiceProvider() {
        return superUserFirebaseApplicationConfigurationServiceProvider;
    }

    @Inject
    public void setSuperUserFirebaseApplicationConfigurationServiceProvider(Provider<SuperUserFirebaseApplicationConfigurationService> superUserFirebaseApplicationConfigurationServiceProvider) {
        this.superUserFirebaseApplicationConfigurationServiceProvider = superUserFirebaseApplicationConfigurationServiceProvider;
    }

}

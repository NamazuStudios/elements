package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.ApplicationService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 7/10/15.
 */
public class ApplicationServiceProvider implements Provider<ApplicationService> {

    private User user;

    private Provider<AnonApplicationService> anonApplicationServiceProvider;

    private Provider<SuperUserApplicationService> superUserApplicationServiceProvider;

    @Override
    public ApplicationService get() {
        switch (getUser().getLevel()) {
        case SUPERUSER:
            return getSuperUserApplicationServiceProvider().get();
        default:
            return getAnonApplicationServiceProvider().get();

        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonApplicationService> getAnonApplicationServiceProvider() {
        return anonApplicationServiceProvider;
    }

    @Inject
    public void setAnonApplicationServiceProvider(Provider<AnonApplicationService> anonApplicationServiceProvider) {
        this.anonApplicationServiceProvider = anonApplicationServiceProvider;
    }

    public Provider<SuperUserApplicationService> getSuperUserApplicationServiceProvider() {
        return superUserApplicationServiceProvider;
    }

    @Inject
    public void setSuperUserApplicationServiceProvider(Provider<SuperUserApplicationService> superUserApplicationServiceProvider) {
        this.superUserApplicationServiceProvider = superUserApplicationServiceProvider;
    }

}

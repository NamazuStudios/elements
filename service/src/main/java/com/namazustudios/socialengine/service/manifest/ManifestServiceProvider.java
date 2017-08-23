package com.namazustudios.socialengine.service.manifest;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.ManifestService;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class ManifestServiceProvider implements Provider<ManifestService> {

    private User user;

    private Provider<SuperUserManifestService> superUserManifestServiceProvider;

    @Override
    public ManifestService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserManifestServiceProvider().get();
            default:
                return Services.forbidden(ManifestService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserManifestService> getSuperUserManifestServiceProvider() {
        return superUserManifestServiceProvider;
    }

    @Inject
    public void setSuperUserManifestServiceProvider(Provider<SuperUserManifestService> superUserManifestServiceProvider) {
        this.superUserManifestServiceProvider = superUserManifestServiceProvider;
    }

}

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

    private Provider<ReadOnlyManifestService> readOnlyManifestServiceProvider;

    @Override
    public ManifestService get() {
        switch (getUser().getLevel()) {
            default: return getReadOnlyManifestServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<ReadOnlyManifestService> getReadOnlyManifestServiceProvider() {
        return readOnlyManifestServiceProvider;
    }

    @Inject
    public void setReadOnlyManifestServiceProvider(Provider<ReadOnlyManifestService> readOnlyManifestServiceProvider) {
        this.readOnlyManifestServiceProvider = readOnlyManifestServiceProvider;
    }

}

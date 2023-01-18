package com.namazustudios.socialengine.service.blockchain.omni;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;
import com.namazustudios.socialengine.service.VaultService;

import javax.inject.Inject;
import javax.inject.Provider;

public class VaultServiceProvider implements Provider<VaultService> {

    private User user;

    private Provider<UserVaultService> userVaultServiceProvider;

    private Provider<SuperUserVaultService> superUserVaultServiceProvider;

    @Override
    public VaultService get() {
        switch (getUser().getLevel()) {
            case USER:
                return getUserVaultServiceProvider().get();
            case SUPERUSER:
                return getSuperUserVaultServiceProvider().get();
            default:
                return Services.forbidden(VaultService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserVaultService> getUserVaultServiceProvider() {
        return userVaultServiceProvider;
    }

    @Inject
    public void setUserVaultServiceProvider(Provider<UserVaultService> userVaultServiceProvider) {
        this.userVaultServiceProvider = userVaultServiceProvider;
    }

    public Provider<SuperUserVaultService> getSuperUserVaultServiceProvider() {
        return superUserVaultServiceProvider;
    }

    @Inject
    public void setSuperUserVaultServiceProvider(Provider<SuperUserVaultService> superUserVaultServiceProvider) {
        this.superUserVaultServiceProvider = superUserVaultServiceProvider;
    }

}

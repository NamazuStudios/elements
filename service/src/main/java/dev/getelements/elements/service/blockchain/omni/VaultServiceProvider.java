package dev.getelements.elements.service.blockchain.omni;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.blockchain.VaultService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

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

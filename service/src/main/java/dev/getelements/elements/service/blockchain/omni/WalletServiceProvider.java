package dev.getelements.elements.service.blockchain.omni;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.blockchain.WalletService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class WalletServiceProvider implements Provider<WalletService> {

    private User user;

    private Provider<UserWalletService> userWalletServiceProvider;

    private Provider<SuperUserWalletService> superUserWalletServiceProvider;

    @Override
    public WalletService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserWalletServiceProvider().get();
            case USER:
                return getUserWalletServiceProvider().get();
            default:
                return Services.forbidden(WalletService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserWalletService> getUserWalletServiceProvider() {
        return userWalletServiceProvider;
    }

    @Inject
    public void setUserWalletServiceProvider(Provider<UserWalletService> userWalletServiceProvider) {
        this.userWalletServiceProvider = userWalletServiceProvider;
    }

    public Provider<SuperUserWalletService> getSuperUserWalletServiceProvider() {
        return superUserWalletServiceProvider;
    }

    @Inject
    public void setSuperUserWalletServiceProvider(Provider<SuperUserWalletService> superUserWalletServiceProvider) {
        this.superUserWalletServiceProvider = superUserWalletServiceProvider;
    }

}

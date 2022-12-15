package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;
import com.namazustudios.socialengine.service.WalletService;

import javax.inject.Inject;
import javax.inject.Provider;

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

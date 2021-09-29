package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class WalletServiceProvider implements Provider<WalletService> {

    private User user;

    private Provider<SuperUserWalletService> superUserWalletService;

    private Provider<UserWalletService> userWalletService;

    @Override
    public WalletService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserWalletService().get();
            case USER:
                return getUserWalletService().get();
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

    public Provider<SuperUserWalletService> getSuperUserWalletService() {
        return superUserWalletService;
    }

    @Inject
    public void setWalletServiceProvider(Provider<SuperUserWalletService> superUserWalletService) {
        this.superUserWalletService = superUserWalletService;
    }

    public Provider<UserWalletService> getUserWalletService() {
        return userWalletService;
    }

    @Inject
    public void setUserWalletService(Provider<UserWalletService> userWalletService) {
        this.userWalletService = userWalletService;
    }
}

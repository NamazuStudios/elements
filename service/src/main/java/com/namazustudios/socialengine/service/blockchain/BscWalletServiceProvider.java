package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class BscWalletServiceProvider implements Provider<BscWalletService> {

    private User user;

    private Provider<SuperUserBscWalletService> superUserWalletService;

    private Provider<UserBscWalletService> userWalletService;

    @Override
    public BscWalletService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserWalletService().get();
            case USER:
                return getUserWalletService().get();
            default:
                return Services.forbidden(BscWalletService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserBscWalletService> getSuperUserWalletService() {
        return superUserWalletService;
    }

    @Inject
    public void setWalletServiceProvider(Provider<SuperUserBscWalletService> superUserWalletService) {
        this.superUserWalletService = superUserWalletService;
    }

    public Provider<UserBscWalletService> getUserWalletService() {
        return userWalletService;
    }

    @Inject
    public void setUserWalletService(Provider<UserBscWalletService> userWalletService) {
        this.userWalletService = userWalletService;
    }
}

package com.namazustudios.socialengine.service.blockchain.neo;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;
import com.namazustudios.socialengine.service.blockchain.omni.SuperUserNeoWalletService;

import javax.inject.Inject;
import javax.inject.Provider;

public class NeoWalletServiceProvider implements Provider<NeoWalletService> {

    private User user;

    private Provider<SuperUserNeoWalletService> superUserWalletService;

    private Provider<UserNeoWalletService> userWalletService;

    @Override
    public NeoWalletService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserWalletService().get();
            case USER:
                return getUserWalletService().get();
            default:
                return Services.forbidden(NeoWalletService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserNeoWalletService> getSuperUserWalletService() {
        return superUserWalletService;
    }

    @Inject
    public void setWalletServiceProvider(Provider<SuperUserNeoWalletService> superUserWalletService) {
        this.superUserWalletService = superUserWalletService;
    }

    public Provider<UserNeoWalletService> getUserWalletService() {
        return userWalletService;
    }

    @Inject
    public void setUserWalletService(Provider<UserNeoWalletService> userWalletService) {
        this.userWalletService = userWalletService;
    }
}

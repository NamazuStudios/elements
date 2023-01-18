package com.namazustudios.socialengine.service.blockchain.bsc;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class BscSmartContractServiceProvider implements Provider<BscSmartContractService> {

    private User user;

    private Provider<SuperUserBscSmartContractService> superUserBscSmartContractService;

    private Provider<UserBscSmartContractService> userBscSmartContractService;

    @Override
    public BscSmartContractService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserBscSmartContractService().get();
            case USER:
                return getUserBscSmartContractService().get();
            default:
                return Services.forbidden(BscSmartContractService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserBscSmartContractService> getUserBscSmartContractService() {
        return userBscSmartContractService;
    }

    @Inject
    public void setUserBscSmartContractServiceProvider(Provider<UserBscSmartContractService> UserBscSmartContractService) {
        this.userBscSmartContractService = UserBscSmartContractService;
    }

    public Provider<SuperUserBscSmartContractService> getSuperUserBscSmartContractService() {
        return superUserBscSmartContractService;
    }

    @Inject
    public void setSuperUserBscSmartContractServiceProvider(Provider<SuperUserBscSmartContractService> superUserBscSmartContractService) {
        this.superUserBscSmartContractService = superUserBscSmartContractService;
    }

}

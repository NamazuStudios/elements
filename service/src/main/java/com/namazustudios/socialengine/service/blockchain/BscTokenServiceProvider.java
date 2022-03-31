package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class BscTokenServiceProvider implements Provider<BscTokenService> {

    private User user;

    private Provider<SuperUserBscTokenService> superUserTokenService;

    private Provider<UserBscTokenService> userTokenService;

    @Override
    public BscTokenService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserTokenService().get();
            case USER:
                return getUserTokenService().get();
            default:
                return Services.forbidden(BscTokenService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }


    public Provider<SuperUserBscTokenService> getSuperUserTokenService() {
        return superUserTokenService;
    }

    @Inject
    public void setSuperUserTokenService(Provider<SuperUserBscTokenService> superUserTokenService) {
        this.superUserTokenService = superUserTokenService;
    }

    public Provider<UserBscTokenService> getUserTokenService() {
        return userTokenService;
    }

    @Inject
    public void setUserTokenService(Provider<UserBscTokenService> userTokenService) {
        this.userTokenService = userTokenService;
    }
}

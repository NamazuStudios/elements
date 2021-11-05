package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class TokenServiceProvider implements Provider<TokenService> {

    private User user;

    private Provider<SuperUserTokenService> superUserTokenService;

    private Provider<UserTokenService> userTokenService;

    @Override
    public TokenService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserTokenService().get();
            case USER:
                return getUserTokenService().get();
            default:
                return Services.forbidden(TokenService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }


    public Provider<SuperUserTokenService> getSuperUserTokenService() {
        return superUserTokenService;
    }

    @Inject
    public void setSuperUserTokenService(Provider<SuperUserTokenService> superUserTokenService) {
        this.superUserTokenService = superUserTokenService;
    }

    public Provider<UserTokenService> getUserTokenService() {
        return userTokenService;
    }

    @Inject
    public void setUserTokenService(Provider<UserTokenService> userTokenService) {
        this.userTokenService = userTokenService;
    }
}

package dev.getelements.elements.service.blockchain.neo;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class NeoTokenServiceProvider implements Provider<NeoTokenService> {

    private User user;

    private Provider<SuperUserNeoTokenService> superUserTokenService;

    private Provider<UserNeoTokenService> userTokenService;

    @Override
    public NeoTokenService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserTokenService().get();
            case USER:
                return getUserTokenService().get();
            default:
                return Services.forbidden(NeoTokenService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }


    public Provider<SuperUserNeoTokenService> getSuperUserTokenService() {
        return superUserTokenService;
    }

    @Inject
    public void setSuperUserTokenService(Provider<SuperUserNeoTokenService> superUserTokenService) {
        this.superUserTokenService = superUserTokenService;
    }

    public Provider<UserNeoTokenService> getUserTokenService() {
        return userTokenService;
    }

    @Inject
    public void setUserTokenService(Provider<UserNeoTokenService> userTokenService) {
        this.userTokenService = userTokenService;
    }
}

package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.GameOnAdminPrizeService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class GameOnAdminPrizeServiceProvider implements Provider<GameOnAdminPrizeService> {

    private User user;

    private Provider<SuperUserGameOnAdminPrizeService> superUserGameOnPrizeServiceProvider;

    @Override
    public GameOnAdminPrizeService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:    return getSuperUserGameOnPrizeServiceProvider().get();
            default:           return forbidden(GameOnAdminPrizeService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserGameOnAdminPrizeService> getSuperUserGameOnPrizeServiceProvider() {
        return superUserGameOnPrizeServiceProvider;
    }

    @Inject
    public void setSuperUserGameOnPrizeServiceProvider(Provider<SuperUserGameOnAdminPrizeService> superUserGameOnPrizeServiceProvider) {
        this.superUserGameOnPrizeServiceProvider = superUserGameOnPrizeServiceProvider;
    }

}

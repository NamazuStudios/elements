package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class TokenTemplateServiceProvider implements Provider<TokenTemplateService> {

    private User user;

    private Provider<SuperUserTokenTemplateService> superUserTokenTemplateService;

    @Override
    public TokenTemplateService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserTokenTemplateService().get();
            default:
                return Services.forbidden(TokenTemplateService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }


    public Provider<SuperUserTokenTemplateService> getSuperUserTokenTemplateService() {
        return superUserTokenTemplateService;
    }

    @Inject
    public void setSuperUserTokenTemplateService(Provider<SuperUserTokenTemplateService> superUserTokenTemplateService) {
        this.superUserTokenTemplateService = superUserTokenTemplateService;
    }
}

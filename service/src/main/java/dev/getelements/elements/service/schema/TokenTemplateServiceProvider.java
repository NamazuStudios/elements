package dev.getelements.elements.service.schema;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class TokenTemplateServiceProvider implements Provider<TokenTemplateService> {

    private User user;

    private Provider<SuperUserTokenTemplateService> superUserTokenTemplateService;

    private Provider<UserTokenTemplateService> userTokenTemplateService;

    @Override
    public TokenTemplateService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserTokenTemplateService().get();
            case USER:
                return getUserTokenTemplateService().get();
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

    public Provider<UserTokenTemplateService> getUserTokenTemplateService() {
        return userTokenTemplateService;
    }

    @Inject
    public void setUserTokenTemplateService(Provider<UserTokenTemplateService> userTokenTemplateService) {
        this.userTokenTemplateService = userTokenTemplateService;
    }
}

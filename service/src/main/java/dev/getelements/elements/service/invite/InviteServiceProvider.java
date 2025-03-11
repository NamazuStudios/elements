package dev.getelements.elements.service.invite;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.invite.InviteService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class InviteServiceProvider implements Provider<InviteService> {

    private User user;

    private Provider<UserInviteService> userInviteService;

    private Provider<SuperUserInviteService> superUserInviteService;

    @Override
    public InviteService get() {
        switch (getUser().getLevel()) {
            case USER:
                return getUserInviteService().get();
            case SUPERUSER:
                return getSuperUserInviteService().get();
            default:
                return Services.forbidden(InviteService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserInviteService> getUserInviteService() {
        return userInviteService;
    }

    @Inject
    public void setUserInviteService(Provider<UserInviteService> userInviteService) {
        this.userInviteService = userInviteService;
    }

    public Provider<SuperUserInviteService> getSuperUserInviteService() {
        return superUserInviteService;
    }

    @Inject
    public void setSuperUserInviteService(Provider<SuperUserInviteService> superUserInviteService) {
        this.superUserInviteService = superUserInviteService;
    }
}
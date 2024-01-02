package dev.getelements.elements.service.invite;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.InviteService;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class InviteServiceProvider implements Provider<InviteService> {

    private User user;

    private Provider<AnonInviteService> anonInviteService;

    private Provider<UserInviteService> userInviteService;

    private Provider<SuperUserInviteService> superUserInviteService;

    @Override
    public InviteService get() {
        switch (getUser().getLevel()) {
            case UNPRIVILEGED:
                return getAnonInviteService().get();
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

    public Provider<AnonInviteService> getAnonInviteService() {
        return anonInviteService;
    }

    @Inject
    public void setAnonInviteService(Provider<AnonInviteService> anonInviteService) {
        this.anonInviteService = anonInviteService;
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
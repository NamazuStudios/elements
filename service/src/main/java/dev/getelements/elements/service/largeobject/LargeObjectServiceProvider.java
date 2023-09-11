package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.LargeObjectService;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class LargeObjectServiceProvider implements Provider<LargeObjectService> {

    private User user;

    private Provider<AnonLargeObjectService> anonLargeObjectServiceProvider;

    private Provider<UserLargeObjectService> userLargeObjectServiceProvider;

    private Provider<SuperUserLargeObjectService> superUserLargeObjectServiceProvider;

    @Override
    public LargeObjectService get() {
        switch (getUser().getLevel()) {
            case UNPRIVILEGED:
                return getAnonLargeObjectServiceProvider().get();
            case USER:
                return getUserLargeObjectServiceProvider().get();
            case SUPERUSER:
                return getSuperUserLargeObjectServiceProvider().get();
            default:
                return Services.forbidden(LargeObjectService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonLargeObjectService> getAnonLargeObjectServiceProvider() {
        return anonLargeObjectServiceProvider;
    }

    @Inject
    public void setAnonLargeObjectServiceProvider(Provider<AnonLargeObjectService> anonLargeObjectServiceProvider) {
        this.anonLargeObjectServiceProvider = anonLargeObjectServiceProvider;
    }

    public Provider<UserLargeObjectService> getUserLargeObjectServiceProvider() {
        return userLargeObjectServiceProvider;
    }

    @Inject
    public void setUserLargeObjectServiceProvider(Provider<UserLargeObjectService> userLargeObjectServiceProvider) {
        this.userLargeObjectServiceProvider = userLargeObjectServiceProvider;
    }

    public Provider<SuperUserLargeObjectService> getSuperUserLargeObjectServiceProvider() {
        return superUserLargeObjectServiceProvider;
    }

    @Inject
    public void setSuperUserLargeObjectServiceProvider(Provider<SuperUserLargeObjectService> superUserLargeObjectServiceProvider) {
        this.superUserLargeObjectServiceProvider = superUserLargeObjectServiceProvider;
    }

}

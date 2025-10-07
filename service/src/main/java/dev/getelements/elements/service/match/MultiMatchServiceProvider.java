package dev.getelements.elements.service.match;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.match.MatchService;
import dev.getelements.elements.sdk.service.match.MultiMatchService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class MultiMatchServiceProvider implements Provider<MultiMatchService> {

    private User user;

    private Provider<UserMultiMatchService> userMatchServiceProvider;

    private Provider<SuperuserMultiMatchService> superuserMatchServiceProvider;

    @Override
    public MultiMatchService get() {
        switch (getUser().getLevel()) {
            case USER:
                return getUserMatchServiceProvider().get();
            case SUPERUSER:
                return getSuperuserMatchServiceProvider().get();
            default:
                return forbidden(MultiMatchService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserMultiMatchService> getUserMatchServiceProvider() {
        return userMatchServiceProvider;
    }

    @Inject
    public void setUserMatchServiceProvider(Provider<UserMultiMatchService> userMatchServiceProvider) {
        this.userMatchServiceProvider = userMatchServiceProvider;
    }

    public Provider<SuperuserMultiMatchService> getSuperuserMatchServiceProvider() {
        return superuserMatchServiceProvider;
    }

    @Inject
    public void setSuperuserMatchServiceProvider(Provider<SuperuserMultiMatchService> superuserMatchServiceProvider) {
        this.superuserMatchServiceProvider = superuserMatchServiceProvider;
    }

}

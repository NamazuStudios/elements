package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.user.UserService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Supplies the appropriate {@link UserService} based on current
 * access level.
 *
 * Created by patricktwohig on 4/2/15.
 */
public class UserServiceProvider implements Provider<UserService> {

    @Inject
    private User user;

    @Inject
    private Provider<SuperuserUserService> superuserUserServiceProvider;

    @Inject
    private Provider<UserUserService> userUserServiceProvider;

    @Inject
    private Provider<AnonUserService> anonUserServiceProvider;

    @Override
    public UserService get() {
        switch (user.getLevel()) {
            case SUPERUSER:
                return superuserUserServiceProvider.get();
            case USER:
                return userUserServiceProvider.get();
            default:
                return anonUserServiceProvider.get();
        }
    }

}

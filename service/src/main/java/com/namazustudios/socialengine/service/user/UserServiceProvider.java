package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.Services;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Supplies the appropriate {@link com.namazustudios.socialengine.service.UserService} based on current
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

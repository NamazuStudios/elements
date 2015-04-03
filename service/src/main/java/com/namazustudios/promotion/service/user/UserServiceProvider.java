package com.namazustudios.promotion.service.user;

import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.Services;
import com.namazustudios.promotion.service.UserService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Supplies the appropriate {@link com.namazustudios.promotion.service.UserService} based on current
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

    @Override
    public UserService get() {
        switch (user.getLevel()) {
            case SUPERUSER:
                return superuserUserServiceProvider.get();
            case USER:
                return userUserServiceProvider.get();
            default:
                return Services.forbidden(UserService.class);
        }
    }

}

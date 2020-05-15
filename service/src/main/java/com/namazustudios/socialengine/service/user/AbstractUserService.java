package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 3/26/15.
 */
public abstract class AbstractUserService implements UserService {

    @Inject
    private User currentUser;

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

}

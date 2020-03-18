package com.namazustudios.socialengine.service.user;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;
import java.util.Objects;

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

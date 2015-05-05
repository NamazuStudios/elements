package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.AuthService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/2/15.
 */
public class UserAuthService implements AuthService {

    @Inject
    private User user;

    @Override
    public User loginUser(String userId, String password) {
        return user;
    }

}

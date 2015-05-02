package com.namazustudios.promotion.service.auth;

import com.namazustudios.promotion.exception.ForbiddenException;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.AuthService;

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

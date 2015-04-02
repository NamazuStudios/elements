package com.namazustudios.promotion.service.auth;

import com.namazustudios.promotion.dao.UserDao;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.AuthService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by patricktwohig on 4/1/15.
 */
@Singleton
public class AnonAuthService implements AuthService  {

    @Inject
    private UserDao userDao;

    @Override
    public User loginUser(String userId, String password) {
        return userDao.validateUserPassword(userId, password);
    }

}

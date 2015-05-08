package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.AuthService;

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
        return userDao.validateActiveUserPassword(userId, password);
    }

}

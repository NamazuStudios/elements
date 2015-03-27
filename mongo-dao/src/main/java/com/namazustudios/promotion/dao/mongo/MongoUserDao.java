package com.namazustudios.promotion.dao.mongo;

import com.namazustudios.promotion.dao.UserDao;
import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.User;

import javax.inject.Singleton;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoUserDao implements UserDao {

    @Override
    public User getUser(String userId) {
        return null;
    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {
        return null;
    }

    @Override
    public User createUser(User user) {
        return null;
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public void deleteUser(String userId) {

    }

    @Override
    public User updateUserPassword(String userId, String password) {
        return null;
    }

}

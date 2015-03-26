package com.namazustudios.promotion.service.user;

import com.namazustudios.promotion.model.PaginatedEntry;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.UserService;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class SuperuserUserService extends AbstractUserService implements UserService {

    @Override
    public User getUser(String userId) {
        return null;
    }

    @Override
    public PaginatedEntry<User> getUsers(int offset, int count) {
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
    public User updateUserPassword(String user, String password) {
        return null;
    }

}

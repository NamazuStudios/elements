package com.namazustudios.promotion.service.user;

import com.google.common.collect.Lists;
import com.namazustudios.promotion.exception.ForbiddenException;
import com.namazustudios.promotion.exception.NotFoundException;
import com.namazustudios.promotion.model.PaginatedEntry;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.UserService;
import org.apache.commons.lang3.ObjectUtils;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class UserUserService extends AbstractUserService implements UserService {

    @Override
    public User getUser(String userId) {
        checkForCurrentUser(userId);
        return getCurrentUser();
    }

    @Override
    public PaginatedEntry<User> getUsers(int offset, int count) {
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        } else if (offset == 0) {
            final PaginatedEntry<User> entry = new PaginatedEntry<>();
            entry.setOffset(0);
            entry.setCount(1);
            entry.setObjects(Lists.newArrayList(getCurrentUser()));
            return entry;
        } else {
            return new PaginatedEntry<>();
        }
    }

    @Override
    public User createUser(User user) {
        throw new ForbiddenException();
    }

    @Override
    public User updateUser(User user) {

        checkForCurrentUser(user.getName());

        // Users cannot change their own level
        user.setLevel(User.Level.USER);
        user.setName(getCurrentUser().getName());

        //TODO update user
        return null;

    }

    @Override
    public void deleteUser(String userId) {
        checkForCurrentUser(userId);
        // TODO Delete the user
    }

    @Override
    public User updateUserPassword(String user, String password) {
        // TODO Update the user's password
        return null;
    }

}

package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.user.User;

import java.util.concurrent.atomic.AtomicInteger;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static java.lang.String.format;

public class UserTestFactory {

    private final AtomicInteger suffix = new AtomicInteger();

    private UserDao userDao;

    public User createTestUser() {
        final var testUser = new User();
        final var userName = format("testy.mctesterson.%d", suffix.getAndIncrement());
        testUser.setName(userName);
        testUser.setEmail(format("%s@example.com", userName));
        testUser.setLevel(USER);
        return getUserDao().createOrReactivateUser(testUser);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}

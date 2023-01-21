package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static java.lang.String.format;

public class UserTestFactory {

    private static final AtomicInteger suffix = new AtomicInteger();

    private UserDao userDao;

    public User createTestUser() {
        return createTestUser(null, true);
    }

    public User createTestUser(boolean addToDB) {
        return createTestUser(null, addToDB);
    }

    public User createTestUser(Consumer<User> precreateConsumer) {
        return createTestUser(precreateConsumer, true);
    }

    public User createTestUser(Consumer<User> precreateConsumer, boolean addToDB) {

        final var testUser = buildTestUser(precreateConsumer);

        if(addToDB) {
            return getUserDao().createOrReactivateUser(testUser);
        }

        return testUser;
    }

    public User buildTestUser() {
        return buildTestUser(u -> {});
    }

    public User buildTestUser(final Consumer<User> precreateConsumer) {

        final var testUser = new User();
        final var userName = format("testy.mctesterson.%d", suffix.getAndIncrement());

        testUser.setName(userName);
        testUser.setEmail(format("%s@example.com", userName));
        testUser.setLevel(USER);
        testUser.setActive(true);

        if(precreateConsumer != null) {
            precreateConsumer.accept(testUser);
        }

        return testUser;

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

}

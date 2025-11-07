package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static dev.getelements.elements.sdk.model.user.User.Level.USER;
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

    public User createTestUser(final Consumer<User> precreateConsumer) {
        return createTestUser(precreateConsumer, true);
    }

    public User createTestUser(final Consumer<User> precreateConsumer, boolean addToDB) {

        final var testUser = buildTestUser(precreateConsumer);

        if(addToDB) {
            return getUserDao().createUser(testUser);
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
        testUser.setLinkedAccounts(Set.of(
                UserUidDao.SCHEME_EMAIL,
                UserUidDao.SCHEME_NAME
        ));

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

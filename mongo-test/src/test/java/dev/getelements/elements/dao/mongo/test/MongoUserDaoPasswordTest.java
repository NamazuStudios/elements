package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.security.PasswordGenerator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoUserDaoPasswordTest {

    private UserDao userDao;

    private UserTestFactory userTestFactory;

    private PasswordGenerator passwordGenerator;

    private Map<String, User> intermediateUsers = new ConcurrentHashMap<>();

    private Map<String, String> intermediateLoginCredentials = new ConcurrentHashMap<>();

    @DataProvider
    private Object[][] getLevels() {
        return Stream.of(User.Level.values())
                .map(l -> new Object[]{l})
                .toArray(Object[][]::new);
    }

    @Test(invocationCount = 20, dataProvider = "getLevels")
    public void testCreateUser(final User.Level level) {

        final var user = getUserTestFactory().buildTestUser(u -> u.setLevel(level));
        final var password = getPasswordGenerator().generate();

        final var created = getUserDao().createUserWithPassword(user, password);

        intermediateUsers.put(created.getId(), created);
        assertNull(intermediateLoginCredentials.put(created.getId(), password));
        assertNull(intermediateLoginCredentials.put(created.getName(), password));
        assertNull(intermediateLoginCredentials.put(created.getEmail(), password));

    }

    @DataProvider
    public Object[][] getLoginCredentials() {
        return intermediateLoginCredentials
                .entrySet()
                .stream()
                .map(e -> new Object[]{ e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    @Test(dependsOnMethods = "testCreateUser", dataProvider = "getLoginCredentials")
    public void testValidatePassword(final String login, final String password) {
        final var user = getUserDao().validateUserPassword(login, password);
        assertEquals(user, intermediateUsers.get(user.getId()));
    }

    @DataProvider
    public Object[][] getUserAndPassword() {
        return intermediateUsers
                .values()
                .stream()
                .map(user -> new Object[]{user, intermediateLoginCredentials.get(user.getId())})
                .toArray(Object[][]::new);
    }

    @Test(dependsOnMethods = "testValidatePassword", dataProvider = "getUserAndPassword")
    public void testUpdatePassword(final User user, final String oldPassword) {

        final var newPassword = getPasswordGenerator().generate();
        final var updatedUser = getUserDao().updateUser(user, newPassword, oldPassword);

        assertEquals(user.getId(), updatedUser.getId());

        intermediateUsers.put(updatedUser.getId(), updatedUser);
        assertNotNull(intermediateLoginCredentials.put(updatedUser.getId(), newPassword));
        assertNotNull(intermediateLoginCredentials.put(updatedUser.getName(), newPassword));
        assertNotNull(intermediateLoginCredentials.put(updatedUser.getEmail(), newPassword));

        Stream.of(
            getUserDao().validateUserPassword(user.getId(), newPassword),
            getUserDao().validateUserPassword(user.getName(), newPassword),
            getUserDao().validateUserPassword(user.getEmail(), newPassword)
        ).forEach(u -> assertEquals(user.getId(), u.getId()));

    }

    @Test(dependsOnMethods = "testCreateUser", dataProvider = "getUserAndPassword", expectedExceptions = NotFoundException.class)
    public void testUpdatePasswordFails(final User user, final String oldPassword) {

        String badPassword;

        do {
            // The likelihood that the generator will ever generate the same password twice is
            // extremely small, but we may as well not have the potentiality of a randomly failing
            // test.
            badPassword = getPasswordGenerator().generate();
        } while(badPassword.equals(oldPassword));

        getUserDao().updateUser(user, getPasswordGenerator().generate(), badPassword);

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    @Inject
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

}

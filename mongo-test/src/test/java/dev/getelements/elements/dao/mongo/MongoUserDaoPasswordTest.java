package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.security.PasswordGenerator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

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

        final var created = getUserDao().createOrReactivateUserWithPassword(user, password);

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
        final var user = getUserDao().validateActiveUserPassword(login, password);
        assertEquals(user, intermediateUsers.get(user.getId()));
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

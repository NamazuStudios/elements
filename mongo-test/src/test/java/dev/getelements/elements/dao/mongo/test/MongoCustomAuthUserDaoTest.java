package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.CustomAuthUserDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.auth.UserClaim;
import dev.getelements.elements.model.auth.UserKey;
import dev.getelements.elements.model.user.User;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

@Guice(modules = IntegrationTestModule.class)
public class MongoCustomAuthUserDaoTest {

    private UserDao userDao;

    private CustomAuthUserDao customAuthUserDao;

    private Map<String, User> intermediateUsers = new ConcurrentHashMap<>();

    public void updateIntermediate(final User user) {
        intermediateUsers.put(user.getId(), user);
    }

    @DataProvider
    public Object[][] getUserPermutations() {

        final var result = new ArrayList<Object[]>();

        for(int iteration = 10000; iteration < 10100; ++iteration) {
            for (var userKey : UserKey.values()) {
                for (var level : User.Level.values()) {
                    result.add(new Object[]{iteration, userKey, level});
                }
            }
        }

        return result.toArray(Object[][]::new);

    }

    @Test(invocationCount = 2, dataProvider = "getUserPermutations")
    public void testUpsertUser(final int iteration, final UserKey userKey, final User.Level level) {

        final var name = format("%s.%s.%d", level, userKey, iteration);
        final var email = format("%s@example.com", name);

        final String subject;

        switch (userKey) {
            case NAME:
                subject = name;
                break;
            case EMAIL:
                subject = email;
                break;
            case FACEBOOK_ID:
            case FIREBASE_ID:
            case APPLE_SIGN_IN_ID:
            case EXTERNAL_USER_ID:
                subject = formatId(userKey, level, iteration);
                break;
            default:
                throw new IllegalArgumentException("Invalid user key: " + userKey);
        }

        final var claim = new UserClaim();

        claim.setName(name);
        claim.setEmail(email);
        claim.setLevel(level);
        claim.setFacebookId(formatId(userKey, level, iteration));
        claim.setFirebaseId(formatId(userKey, level, iteration));
        claim.setAppleSignInId(formatId(userKey, level, iteration));
        claim.setExternalUserId(formatId(userKey, level, iteration));

        final var user = getCustomAuthUserDao().upsertUser(userKey, subject, claim);

        assertEquals(user.getName(), claim.getName());
        assertEquals(user.getEmail(), claim.getEmail());
        assertEquals(user.getLevel(), claim.getLevel());
        assertEquals(user.getFacebookId(), claim.getFacebookId());
        assertEquals(user.getFirebaseId(), claim.getFirebaseId());
        assertEquals(user.getAppleSignInId(), claim.getAppleSignInId());
        assertEquals(user.getExternalUserId(), claim.getExternalUserId());

        updateIntermediate(user);

    }

    private String formatId(final UserKey userKey, final User.Level level, final int iteration) {
        return format("%s:%s:%05d", userKey, level, iteration);
    }

    @Test(dataProvider = "getUserPermutations", dependsOnMethods = "testUpsertUser")
    public void testCrossValidation(final int iteration, final UserKey userKey, final User.Level level) {

        final var name = format("%s.%s.%d", level, userKey, iteration);
        final var email = format("%s@example.com", name);

        final String subject;

        switch (userKey) {
            case NAME:
                subject = name;
                break;
            case EMAIL:
                subject = email;
                break;
            case FACEBOOK_ID:
            case FIREBASE_ID:
            case APPLE_SIGN_IN_ID:
            case EXTERNAL_USER_ID:
                subject = formatId(userKey, level, iteration);
                break;
            default:
                throw new IllegalArgumentException("Invalid user key: " + userKey);
        }

        final var customAuthUserDaoUser = getCustomAuthUserDao().getActiveUser(userKey, subject);
        final var intermediateUser = intermediateUsers.get(customAuthUserDaoUser.getId());
        final var userDaoUser = getUserDao().getActiveUser(intermediateUser.getId());

        assertEquals(customAuthUserDaoUser, intermediateUser);
        assertEquals(userDaoUser, intermediateUser);
        assertEquals(customAuthUserDaoUser, userDaoUser);

    }

    @DataProvider
    public Object[][] getIntermediateUserNames() {
        return intermediateUsers
            .values()
            .stream()
            .flatMap(u -> Stream.of(u.getId(), u.getName(), u.getEmail()))
            .map(s -> new Object[]{s})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getIntermediateUserNames", expectedExceptions = ForbiddenException.class)
    public void testUserHasPassword(final String login) {
        // This tests that the database has a valid password hash for every user we created, even if we don't know the
        // password we must be able to validate that attempting to check the password doesn't result in any unexpected
        // exceptions (eg NullPointerException).
        getUserDao().validateActiveUserPassword(login, "");
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public CustomAuthUserDao getCustomAuthUserDao() {
        return customAuthUserDao;
    }

    @Inject
    public void setCustomAuthUserDao(CustomAuthUserDao customAuthUserDao) {
        this.customAuthUserDao = customAuthUserDao;
    }

}

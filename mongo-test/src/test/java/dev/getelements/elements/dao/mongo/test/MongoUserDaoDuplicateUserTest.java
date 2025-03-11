//package dev.getelements.elements.dao.mongo.test;
//
//import dev.getelements.elements.sdk.dao.UserDao;
//import dev.getelements.elements.sdk.model.exception.DuplicateException;
//import org.testng.annotations.Guice;
//import org.testng.annotations.Test;
//
//import jakarta.inject.Inject;
//import java.util.Random;
//
//import static java.lang.Character.toLowerCase;
//import static java.lang.Character.toUpperCase;
//
//@Guice(modules = IntegrationTestModule.class)
//public class MongoUserDaoDuplicateUserTest {
//
//    private UserDao userDao;
//
//    private UserTestFactory userTestFactory;
//
//    @Test(expectedExceptions = DuplicateException.class)
//    public void testDuplicateUser() {
//        final var user = getUserTestFactory().createTestUser();
//        getUserDao().createOrReactivateUser(user);
//    }
//
//    @Test(expectedExceptions = DuplicateException.class)
//    public void testDuplicateUserCaseEmailUpper() {
//        final var user = getUserTestFactory().createTestUser();
//        user.setEmail(user.getEmail().toUpperCase());
//        getUserDao().createOrReactivateUser(user);
//    }
//
//    @Test(expectedExceptions = DuplicateException.class)
//    public void testDuplicateUserCaseEmailLower() {
//        final var user = getUserTestFactory().createTestUser();
//        user.setEmail(user.getEmail().toLowerCase());
//        getUserDao().createOrReactivateUser(user);
//    }
//
//    @Test(expectedExceptions = DuplicateException.class)
//    public void testDuplicateUserCaseEmailMonteCarloScrambled() {
//        final var user = getUserTestFactory().createTestUser();
//        final var email = scrambleCase(user.getEmail());
//        user.setEmail(email);
//        getUserDao().createOrReactivateUser(user);
//    }
//
//    @Test(expectedExceptions = DuplicateException.class)
//    public void testDuplicateUserCaseNameUpper() {
//        final var user = getUserTestFactory().createTestUser();
//        user.setName(user.getName().toUpperCase());
//        getUserDao().createOrReactivateUser(user);
//    }
//
//    @Test(expectedExceptions = DuplicateException.class)
//    public void testDuplicateUserCaseNameLower() {
//        final var user = getUserTestFactory().createTestUser();
//        user.setName(user.getName().toLowerCase());
//        getUserDao().createOrReactivateUser(user);
//    }
//
//    @Test(expectedExceptions = DuplicateException.class)
//    public void testDuplicateUserCaseNameMonteCarloScrambled() {
//        final var user = getUserTestFactory().createTestUser();
//        final var name = scrambleCase(user.getName());
//        user.setName(name);
//        getUserDao().createOrReactivateUser(user);
//    }
//
//    private static final String scrambleCase(final String input) {
//
//        final var rd = new Random();
//        final var sb = new StringBuilder();
//
//        for (int i = 0; i < input.length(); ++i) {
//            final var ch = rd.nextBoolean() ? toUpperCase(input.charAt(i)) : toLowerCase(input.charAt(i));
//            sb.append(ch);
//        }
//
//        return sb.toString();
//
//    }
//
//    public UserDao getUserDao() {
//        return userDao;
//    }
//
//    @Inject
//    public void setUserDao(UserDao userDao) {
//        this.userDao = userDao;
//    }
//
//    public UserTestFactory getUserTestFactory() {
//        return userTestFactory;
//    }
//
//    @Inject
//    public void setUserTestFactory(UserTestFactory userTestFactory) {
//        this.userTestFactory = userTestFactory;
//    }
//
//}

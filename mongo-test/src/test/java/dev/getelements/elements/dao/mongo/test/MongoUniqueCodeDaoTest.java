package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.UniqueCodeDao;
import jakarta.inject.Inject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.smallrye.common.constraint.Assert.assertFalse;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoUniqueCodeDaoTest {

    private UniqueCodeDao uniqueCodeDao;

    private UserTestFactory userTestFactory;;

    private ProfileTestFactory profileTestFactory;

    private ApplicationTestFactory applicationTestFactory;

    private final Set<String> generated = ConcurrentHashMap.newKeySet();

    @DataProvider(parallel = true)
    public Object[][] lengths() {
        return new Object[][] {
            {4}, {6}, {8}, {10}, {12}, {14}, {16}
        };
    }

    @DataProvider(parallel = true)
    public Object[][] codes() {
        return generated.stream()
                .map(code -> new Object[] { code })
                .toArray(Object[][]::new);
    }

    @Test(groups = "createUniqueCode", dataProvider = "lengths", invocationCount = 500)
    public void testCreateUniqueCode(final int length) {

        final var parameters = UniqueCodeDao.GenerationParameters.withLength(length);
        final var code = getUniqueCodeDao().generateCode(parameters);

        assertEquals(length, code.getId().length());
        assertTrue(generated.add(code.getId()), "Expected code to be unique.");
        assertTrue(code.getExpiry() > 0, "Expected expiry to be greater than zero.");
        assertEquals(parameters.linger(), code.getLinger(), "Expected linger to match.");
        assertEquals(parameters.timeout(), code.getTimeout(), "Expected timeout to match.");
        assertNull(code.getUser(), "Expected user to be null.");
        assertNull(code.getProfile(), "Expected profile to be null.");

    }

    @Test(groups = "createUniqueCode", dataProvider = "lengths")
    public void testCreateUniqueForUser(final int length) {

        final var user = getUserTestFactory().createTestUser();
        final var parameters = UniqueCodeDao.GenerationParameters.withLength(length, user);
        final var code = getUniqueCodeDao().generateCode(parameters);

        assertEquals(length, code.getId().length());
        assertTrue(generated.add(code.getId()), "Expected code to be unique.");
        assertTrue(code.getExpiry() > 0, "Expected expiry to be greater than zero.");
        assertEquals(parameters.linger(), code.getLinger(), "Expected linger to match.");
        assertEquals(parameters.timeout(), code.getTimeout(), "Expected timeout to match.");
        assertEquals(user.getId(), code.getUser().getId(), "Expected user to be null.");
        assertNull(code.getProfile(), "Expected profile to be null.");

    }

    @Test(groups = "createUniqueCode", dataProvider = "lengths")
    public void testCreateUniqueForProfile(final int length) {

        final var user = getUserTestFactory().createTestUser();
        final var application = getApplicationTestFactory().createMockApplication(MongoUniqueCodeDaoTest.class);
        final var profile = getProfileTestFactory().makeMockProfile(user, application);
        final var parameters = UniqueCodeDao.GenerationParameters.withLength(length, profile);

        final var code = getUniqueCodeDao().generateCode(parameters);

        assertEquals(length, code.getId().length());
        assertTrue(generated.add(code.getId()), "Expected code to be unique.");
        assertTrue(code.getExpiry() > 0, "Expected expiry to be greater than zero.");
        assertEquals(parameters.linger(), code.getLinger(), "Expected linger to match.");
        assertEquals(parameters.timeout(), code.getTimeout(), "Expected timeout to match.");
        assertEquals(profile.getUser().getId(), code.getUser().getId(), "Expected user to be null.");
        assertEquals(profile.getId(), code.getProfile().getId(), "Expected profile to be null.");

    }

    @Test(groups = "getUniqueCode", dataProvider = "codes", dependsOnGroups = "createUniqueCode")
    public void testGetUniqueCode(final String code) {
        final var uniqueCode = getUniqueCodeDao().getCode(code);
        assertEquals(code, uniqueCode.getId());
        assertTrue(uniqueCode.getExpiry() > 0, "Expected expiry to be greater than zero.");
    }

    @Test(groups = "getUniqueCode", dataProvider = "codes", dependsOnGroups = "createUniqueCode")
    public void testFindUniqueCode(final String code) {

        final var foundCode = getUniqueCodeDao().findCode(code);

        assertTrue(foundCode.isPresent(), "Expected to find generated code.");

        final var uniqueCode = foundCode.get();
        assertEquals(code, uniqueCode.getId());
        assertTrue(uniqueCode.getExpiry() > 0, "Expected expiry to be greater than zero.");

    }

    @Test(groups = "getUniqueCode", dataProvider = "codes", dependsOnGroups = "createUniqueCode")
    public void testResetTimeout(final String code) {

        final var foundCode = getUniqueCodeDao().findCode(code);
        assertTrue(foundCode.isPresent(), "Expected to find generated code.");
        assertEquals(code, foundCode.get().getId(), "Expected code to match.");

        final var uniqueCode = getUniqueCodeDao().getCode(code);
        assertEquals(code, uniqueCode.getId());

    }

    @Test(groups = "releaseUniqueCode", dataProvider = "codes", dependsOnGroups = "getUniqueCode")
    public void testReleaseUniqueCode(final String code) {
        getUniqueCodeDao().releaseCode(code);
        assertFalse(getUniqueCodeDao().tryReleaseCode(code));
        assertFalse(getUniqueCodeDao().findCode(code).isPresent());
    }

    public UniqueCodeDao getUniqueCodeDao() {
        return uniqueCodeDao;
    }

    @Inject
    public void setUniqueCodeDao(UniqueCodeDao uniqueCodeDao) {
        this.uniqueCodeDao = uniqueCodeDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public ProfileTestFactory getProfileTestFactory() {
        return profileTestFactory;
    }

    @Inject
    public void setProfileTestFactory(ProfileTestFactory profileTestFactory) {
        this.profileTestFactory = profileTestFactory;
    }

    public ApplicationTestFactory getApplicationTestFactory() {
        return applicationTestFactory;
    }

    @Inject
    public void setApplicationTestFactory(ApplicationTestFactory applicationTestFactory) {
        this.applicationTestFactory = applicationTestFactory;
    }
}

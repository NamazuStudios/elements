package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.exception.profile.ProfileLimitExceededException;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Named;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.dao.ProfileDao.PROFILE_CREATED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@Guice(modules = IntegrationTestModule.class)
public class MongoProfileSlotDaoTest {

    private static final int MAX_PROFILES = 2;

    private ApplicationDao applicationDao;

    private ProfileDao profileDao;

    private UserTestFactory userTestFactory;

    private ApplicationTestFactory applicationTestFactory;

    private Provider<Transaction> transactionProvider;

    @Inject
    @Named(ROOT)
    private ElementRegistry elementRegistry;

    private final Set<String> createdEventIds = ConcurrentHashMap.newKeySet();

    private Application limitedApplication;

    private User user;

    @BeforeClass
    public void setupEventHandlers() {
        elementRegistry.onEvent(ev -> {
            if (PROFILE_CREATED.equals(ev.getEventName())) {
                createdEventIds.add(ev.getEventArgument(0, Profile.class).getId());
            }
        });
    }

    @BeforeClass
    public void setup() {

        final var application = getApplicationTestFactory()
                .createAtomicApplication("Application for MongoProfileSlotDaoTest");
        application.setMaxProfiles(MAX_PROFILES);
        application.setAutoCreateProfile(true);

        limitedApplication = getApplicationDao().createApplication(application);
        user = getUserTestFactory().createTestUser();

    }

    @Test
    public void testCreateSlottedProfileAssignsSlotsUpToLimit() {

        final var first = getProfileDao().createSlottedProfile(newProfile(user, limitedApplication, "Primary"), null);
        assertNotNull(first.getId());
        assertTrue(createdEventIds.contains(first.getId()), "Expected PROFILE_CREATED event for " + first.getId());

        final var primary = getProfileDao().getPrimaryProfile(user.getId(), limitedApplication.getId());
        assertEquals(primary.getId(), first.getId());

        final var second = getProfileDao().createSlottedProfile(newProfile(user, limitedApplication, "Secondary"), null);
        assertNotNull(second.getId());
        assertNotEquals(second.getId(), first.getId());

        // The primary profile (slot 0) is unaffected by additional slots being assigned.
        final var primaryAgain = getProfileDao().getPrimaryProfile(user.getId(), limitedApplication.getId());
        assertEquals(primaryAgain.getId(), first.getId());

    }

    @Test(dependsOnMethods = "testCreateSlottedProfileAssignsSlotsUpToLimit")
    public void testCreateSlottedProfileOverLimitThrows() {
        expectThrows(ProfileLimitExceededException.class,
                () -> getProfileDao().createSlottedProfile(newProfile(user, limitedApplication, "Overflow"), null));
    }

    @Test(dependsOnMethods = "testCreateSlottedProfileOverLimitThrows")
    public void testLoweringMaxProfilesDoesNotAffectExistingProfiles() {

        limitedApplication.setMaxProfiles(1);
        getApplicationDao().updateApplication(limitedApplication);

        // Existing profiles created before the limit was lowered are left alone -- both remain findable.
        final var primary = getProfileDao().getPrimaryProfile(user.getId(), limitedApplication.getId());
        assertNotNull(primary.getId());

        // Restore the limit so later tests in this class see the originally configured value.
        limitedApplication.setMaxProfiles(MAX_PROFILES);
        getApplicationDao().updateApplication(limitedApplication);

    }

    @Test
    public void testFindPrimaryProfileEmptyWhenNoneExists() {

        final var otherUser = getUserTestFactory().createTestUser();

        final var found = getProfileDao().findPrimaryProfile(otherUser.getId(), limitedApplication.getId());
        assertFalse(found.isPresent());

        expectThrows(ProfileNotFoundException.class,
                () -> getProfileDao().getPrimaryProfile(otherUser.getId(), limitedApplication.getId()));

    }

    @Test
    public void testConcurrentCreateSlottedProfileRespectsLimit() throws InterruptedException {

        final var application = getApplicationTestFactory()
                .createAtomicApplication("Concurrent application for MongoProfileSlotDaoTest");
        application.setMaxProfiles(MAX_PROFILES);
        application.setAutoCreateProfile(true);

        final var concurrentApplication = getApplicationDao().createApplication(application);
        final var concurrentUser = getUserTestFactory().createTestUser();

        final var attempts = MAX_PROFILES * 4;
        final var succeeded = new CopyOnWriteArrayList<Profile>();
        final var rejected = new CopyOnWriteArrayList<ProfileLimitExceededException>();

        final ExecutorService executorService = Executors.newFixedThreadPool(attempts);

        try {

            final var futures = IntStream.range(0, attempts)
                    .mapToObj(i -> executorService.submit(() -> {
                        try {
                            final var profile = getTransactionProvider().get().performAndClose(tx ->
                                    tx.getDao(ProfileDao.class).createSlottedProfile(
                                            newProfile(concurrentUser, concurrentApplication, "Slot-" + i),
                                            null));
                            succeeded.add(profile);
                        } catch (ProfileLimitExceededException ex) {
                            rejected.add(ex);
                        }
                    }))
                    .toList();

            for (final var future : futures) {
                try {
                    future.get();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

        } finally {
            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        }

        assertEquals(succeeded.size(), MAX_PROFILES);
        assertEquals(rejected.size(), attempts - MAX_PROFILES);

        final List<String> distinctProfileIds = succeeded.stream().map(Profile::getId).distinct().toList();
        assertEquals(distinctProfileIds.size(), MAX_PROFILES);

        assertTrue(getProfileDao()
                .findPrimaryProfile(concurrentUser.getId(), concurrentApplication.getId())
                .isPresent());

    }

    private Profile newProfile(final User user, final Application application, final String displayName) {
        final var profile = new Profile();
        profile.setUser(user);
        profile.setApplication(application);
        profile.setDisplayName(displayName);
        return profile;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public ApplicationTestFactory getApplicationTestFactory() {
        return applicationTestFactory;
    }

    @Inject
    public void setApplicationTestFactory(ApplicationTestFactory applicationTestFactory) {
        this.applicationTestFactory = applicationTestFactory;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}

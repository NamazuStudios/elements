package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.mongo.migration.MigrationRunner;
import dev.getelements.elements.dao.mongo.migration.migrations.BackfillProfileSlotZeroMigration;
import dev.getelements.elements.dao.mongo.model.profile.MongoProfileSlot;
import dev.getelements.elements.dao.mongo.model.profile.MongoProfileSlotId;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.morphia.Datastore;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import static dev.morphia.query.filters.Filters.eq;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Guice(modules = IntegrationTestModule.class)
public class MigrationRunnerTest {

    private Datastore datastore;

    private ProfileDao profileDao;

    private MigrationRunner migrationRunner;

    private UserTestFactory userTestFactory;

    private ApplicationTestFactory applicationTestFactory;

    private ProfileTestFactory profileTestFactory;

    private User user;

    private Application application;

    private Profile legacyProfile;

    @BeforeClass
    public void setup() {

        user = getUserTestFactory().createTestUser();

        application = getApplicationTestFactory()
                .createMockApplication("Application for MigrationRunnerTest");

        // Simulates a pre-#11 profile: created without any MongoProfileSlot ever having been assigned.
        legacyProfile = getProfileTestFactory().makeMockProfile(user, application);

    }

    @Test
    public void testLegacyProfileHasNoSlotBeforeMigration() {
        assertNull(findSlotZero());
        assertFalse(getMigrationRunner().getAppliedIds().contains(BackfillProfileSlotZeroMigration.ID));
        assertTrue(getMigrationRunner()
                .getPending()
                .stream()
                .anyMatch(m -> BackfillProfileSlotZeroMigration.ID.equals(m.getId())));
    }

    @Test(dependsOnMethods = "testLegacyProfileHasNoSlotBeforeMigration")
    public void testRunBackfillsSlotZero() {

        getMigrationRunner().run();

        final var slot = findSlotZero();
        assertEquals(slot.getProfileId(), new ObjectId(legacyProfile.getId()));

        final var primary = getProfileDao().findPrimaryProfile(user.getId(), application.getId());
        assertTrue(primary.isPresent());
        assertEquals(primary.get().getId(), legacyProfile.getId());

        assertTrue(getMigrationRunner().getAppliedIds().contains(BackfillProfileSlotZeroMigration.ID));
        assertFalse(getMigrationRunner()
                .getPending()
                .stream()
                .anyMatch(m -> BackfillProfileSlotZeroMigration.ID.equals(m.getId())));

    }

    @Test(dependsOnMethods = "testRunBackfillsSlotZero")
    public void testRerunIsNoOp() {
        getMigrationRunner().run();
        final var slot = findSlotZero();
        assertEquals(slot.getProfileId(), new ObjectId(legacyProfile.getId()));
    }

    private MongoProfileSlot findSlotZero() {

        final var slotId = new MongoProfileSlotId(
                new ObjectId(user.getId()),
                new ObjectId(application.getId()),
                0);

        return getDatastore()
                .find(MongoProfileSlot.class)
                .filter(eq("_id", slotId))
                .first();

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public MigrationRunner getMigrationRunner() {
        return migrationRunner;
    }

    @Inject
    public void setMigrationRunner(MigrationRunner migrationRunner) {
        this.migrationRunner = migrationRunner;
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

    public ProfileTestFactory getProfileTestFactory() {
        return profileTestFactory;
    }

    @Inject
    public void setProfileTestFactory(ProfileTestFactory profileTestFactory) {
        this.profileTestFactory = profileTestFactory;
    }

}

package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Regression coverage for #169: soft-deleting a profile must clear the profile link from every session
 * carrying it. The auth filter resolves the request scope's current profile directly from the session's
 * stored profile reference, so a session whose link was left pointing at the soft-deleted profile kept
 * resolving the deleted profile as current on every subsequent request.
 */
@Guice(modules = IntegrationTestModule.class)
public class MongoProfileSoftDeleteSessionLinkTest {

    private UserDao userDao;

    private ApplicationDao applicationDao;

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private UserTestFactory userTestFactory;

    private User user;

    private Profile profile;

    @BeforeClass
    public void setup() {

        final var application = new Application();
        application.setName("profile_soft_delete_session_test_" + randomUUID().toString().replace("-", ""));
        application.setDescription("A mock application.");

        user = getUserTestFactory().createTestUser();

        profile = new Profile();
        profile.setUser(user);
        profile.setApplication(applicationDao.createOrUpdateInactiveApplication(application));
        profile.setDisplayName("display-name-" + user.getName());

        this.profile = getProfileDao().createOrReactivateProfile(profile);

    }

    @Test
    public void softDeleteProfileClearsSessionLink() {

        final var session = new Session();
        session.setUser(user);
        session.setProfile(profile);
        session.setApplication(profile.getApplication());
        session.setExpiry(currentTimeMillis() + 600_000);

        final SessionCreation creation = getSessionDao().create(session);
        assertNotNull(creation.getSessionSecret());

        // Precondition: the live session resolves the profile it was created with.
        final var before = getSessionDao().getSessionBySessionId(creation.getSessionSecret());
        assertNotNull(before.getProfile());
        assertEquals(before.getProfile().getId(), profile.getId());

        getProfileDao().softDeleteProfile(profile.getId());

        // The session must no longer resolve the soft-deleted profile.
        final var after = getSessionDao().getSessionBySessionId(creation.getSessionSecret());
        assertNull(after.getProfile(), "session must not resolve a soft-deleted profile");

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
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

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

}

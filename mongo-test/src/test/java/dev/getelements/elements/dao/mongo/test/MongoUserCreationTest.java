package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import jakarta.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static org.testng.Assert.*;

/**
 * Covers {@link UserDao#newEmptyUser()}, the builder used by identity-provider integrations (e.g. OIDC)
 * to create a user together with explicitly-requested {@link dev.getelements.elements.sdk.model.user.UserUid}
 * links, without the automatic email/name/phone linking that {@link UserDao#createUserStrict(User)} performs.
 */
@Guice(modules = IntegrationTestModule.class)
public class MongoUserCreationTest {

    private UserDao userDao;

    private UserUidDao userUidDao;

    @Test
    public void testCreateWithNoUidsLinksNothing() {

        final var email = randomEmail();

        final var user = userDao.newEmptyUser()
                .level(USER)
                .email(email)
                .create();

        assertNotNull(user.getId());
        assertEquals(user.getEmail(), email);

        // Unlike createUserStrict, newEmptyUser() never auto-links a UserUid for the email.
        assertTrue(userUidDao.findUserUid(email, UserUidDao.SCHEME_EMAIL).isEmpty());
    }

    @Test
    public void testCreateWithUidsLinksExactlyWhatWasRequested() {

        final var email = randomEmail();
        final var oidcScheme = "test.oidc.scheme";
        final var oidcId = UUID.randomUUID().toString();

        final var user = userDao.newEmptyUser()
                .level(USER)
                .email(email)
                .displayName("patdoe")
                .firstName("Pat")
                .lastName("Doe")
                .linkedAccountProfile(oidcScheme, Map.of("given_name", "Pat"))
                .uid(oidcScheme, oidcId, VerificationStatus.VERIFIED)
                .uid(UserUidDao.SCHEME_EMAIL, email, VerificationStatus.VERIFIED)
                .create();

        assertNotNull(user.getId());
        assertEquals(user.getEmail(), email);
        assertEquals(user.getDisplayName(), "patdoe");
        assertEquals(user.getFirstName(), "Pat");
        assertEquals(user.getLastName(), "Doe");
        assertEquals(user.getLinkedAccountProfiles().get(oidcScheme).get("given_name"), "Pat");
        assertTrue(user.getLinkedAccounts().contains(oidcScheme));
        assertTrue(user.getLinkedAccounts().contains(UserUidDao.SCHEME_EMAIL));

        final var oidcUid = userUidDao.getUserUid(oidcId, oidcScheme);
        assertEquals(oidcUid.getUserId(), user.getId());
        assertEquals(oidcUid.getVerificationStatus(), VerificationStatus.VERIFIED);

        final var emailUid = userUidDao.getUserUid(email, UserUidDao.SCHEME_EMAIL);
        assertEquals(emailUid.getUserId(), user.getId());
        assertEquals(emailUid.getVerificationStatus(), VerificationStatus.VERIFIED);
    }

    @Test
    public void testCreateFailsWhenUidAlreadyBelongsToAnotherUser() {

        final var scheme = "test.oidc.scheme.conflict";
        final var sharedId = UUID.randomUUID().toString();

        final var first = userDao.newEmptyUser()
                .level(USER)
                .email(randomEmail())
                .uid(scheme, sharedId, VerificationStatus.VERIFIED)
                .create();

        assertNotNull(first.getId());

        try {
            userDao.newEmptyUser()
                    .level(USER)
                    .email(randomEmail())
                    .uid(scheme, sharedId, VerificationStatus.VERIFIED)
                    .create();
            fail("Expected a DuplicateException when a UserUid is already linked to another user.");
        } catch (final DuplicateException expected) {
            // expected: the second create() must not silently steal the first user's identity link.
        }

        // The first user's link must be untouched by the failed second attempt.
        final var uid = userUidDao.getUserUid(sharedId, scheme);
        assertEquals(uid.getUserId(), first.getId());
    }

    private static String randomEmail() {
        return "mongo-user-creation-" + UUID.randomUUID() + "@example.com";
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
    }

}

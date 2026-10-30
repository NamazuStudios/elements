package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import java.util.UUID;

import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Regression coverage for the create/decode pairing of {@link dev.getelements.elements.dao.mongo.MongoSessionDao}
 * (issue reported from a downstream deployment): since sessions moved to signed tokens, {@code create} mints a
 * bare 32-hex server-side session id. That credential must round-trip through the same DAO — resolve via
 * {@code getBySessionSecret}, slide via {@code refresh}, and revoke via {@code blacklist} — which is exactly what
 * a direct {@code SessionDao.create} caller (an exported SDK surface) relies on. Before the fix, the bare id was
 * rejected by the legacy self-describing-secret parser and every minted session was dead on arrival (401).
 */
@Guice(modules = IntegrationTestModule.class)
public class MongoSessionDaoSecretShapeTest {

    private SessionDao sessionDao;

    private UserDao userDao;

    private User user;

    @BeforeClass
    public void createUser() {
        final var toCreate = new User();
        toCreate.setName("session-shape-" + UUID.randomUUID());
        toCreate.setEmail(toCreate.getName() + "@example.com");
        toCreate.setLevel(USER);
        user = getUserDao().createUser(toCreate);
    }

    private SessionCreation createSession() {
        final var session = new Session();
        session.setUser(getUserDao().getUser(user.getId()));
        session.setExpiry(currentTimeMillis() + SECONDS.toMillis(300));
        return getSessionDao().create(session);
    }

    @Test
    public void mintedSecretResolvesThroughTheDao() {

        final var sessionCreation = createSession();
        final var secret = sessionCreation.getSessionSecret();

        assertNotNull(secret);
        assertEquals(secret.length(), 32, "create mints a bare 32-hex server-side session id.");

        final var resolved = getSessionDao().getBySessionSecret(secret);

        assertEquals(resolved.getUser().getId(), user.getId(),
                "The minted secret must resolve the session record that created it.");

    }

    @Test
    public void mintedSecretRefreshes() {

        final var sessionCreation = createSession();
        final var secret = sessionCreation.getSessionSecret();
        final var newExpiry = currentTimeMillis() + SECONDS.toMillis(600);

        final var refreshed = getSessionDao().refresh(secret, newExpiry);

        assertEquals(refreshed.getUser().getId(), user.getId());
        assertTrue(Math.abs(refreshed.getExpiry() - newExpiry) < SECONDS.toMillis(5),
                "refresh must slide the session's expiry.");

    }

    @Test
    public void mintedSecretBlacklists() {

        final var sessionCreation = createSession();
        final var secret = sessionCreation.getSessionSecret();

        getSessionDao().blacklist(secret);

        assertThrows(dev.getelements.elements.sdk.model.exception.security.NoSessionException.class,
                () -> getSessionDao().getBySessionSecret(secret));
    }

    @Test
    public void garbageSecretStillFailsCleanly() {
        assertThrows(dev.getelements.elements.sdk.model.exception.security.BadSessionSecretException.class,
                () -> getSessionDao().getBySessionSecret("not-a-valid-session-secret"));
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

}

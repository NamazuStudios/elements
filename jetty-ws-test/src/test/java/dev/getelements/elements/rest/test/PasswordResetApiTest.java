package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.dao.PasswordResetTokenDao;
import dev.getelements.elements.sdk.model.user.CompletePasswordResetRequest;
import dev.getelements.elements.sdk.model.user.PasswordResetRequest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;

/**
 * Integration tests for the password reset REST endpoints.
 *
 * <p>Tests {@code POST /user/password/reset/request} and {@code POST /user/password/reset/complete}.
 * Token setup is done directly via {@link PasswordResetTokenDao} to keep the tests self-contained
 * without requiring a live SMTP server.
 *
 * <p><b>Ordering note:</b> Tests that call {@code completeReset} change the user's {@code passwordHash},
 * which invalidates all existing sessions. Those tests must run last; {@code dependsOnMethods} enforces
 * this. See MEMORY.md for the broader pattern.
 */
public class PasswordResetApiTest {

    private static final String NEW_PASSWORD = "n3wP@ssw0rd-reset";

    @Factory
    public Object[] getTests() {
        return new Object[]{TestUtils.getInstance().getTestFixture(PasswordResetApiTest.class)};
    }

    @Inject @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    @Inject
    private PasswordResetTokenDao tokenDao;

    private String sessionSecretBeforeReset;

    @BeforeClass
    public void setup() {
        clientContext.createUser("password-reset-api-test")
                     .createSession();
        sessionSecretBeforeReset = clientContext.getSessionSecret();
    }

    // ---------- POST /user/password/reset/request ----------

    /**
     * Unknown email → 204; no user enumeration regardless of whether the email is registered.
     */
    @Test
    public void requestReset_unknownEmail_returns204() {
        final var body = new PasswordResetRequest();
        body.setEmail("nobody-at-all@not-registered.example.com");

        final var response = client
                .target(apiRoot + "/user/password/reset/request")
                .request(APPLICATION_JSON)
                .post(Entity.entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 204);
    }

    /**
     * Missing email field → 400 (validation failure before the service is reached).
     */
    @Test
    public void requestReset_missingEmail_returns400() {
        final var response = client
                .target(apiRoot + "/user/password/reset/request")
                .request(APPLICATION_JSON)
                .post(Entity.entity("{}", APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);
    }

    /**
     * Malformed email → 400 (Jakarta validation rejects it).
     */
    @Test
    public void requestReset_malformedEmail_returns400() {
        final var body = new PasswordResetRequest();
        body.setEmail("not-an-email-address");

        final var response = client
                .target(apiRoot + "/user/password/reset/request")
                .request(APPLICATION_JSON)
                .post(Entity.entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);
    }

    /**
     * No authentication header required — the endpoint is fully public.
     */
    @Test
    public void requestReset_noAuthRequired() {
        final var body = new PasswordResetRequest();
        body.setEmail("anon-caller@example.com");

        // Deliberately omit any session header
        final var response = client
                .target(apiRoot + "/user/password/reset/request")
                .request(APPLICATION_JSON)
                .post(Entity.entity(body, APPLICATION_JSON));

        // Must not return 401 or 403 — 204 is correct for a void endpoint
        assertEquals(response.getStatus(), 204);
    }

    // ---------- POST /user/password/reset/complete ----------

    /**
     * Unknown token → 400.
     */
    @Test
    public void completeReset_unknownToken_returns400() {
        final var body = new CompletePasswordResetRequest();
        body.setToken("no-such-token-in-the-database");
        body.setPassword(NEW_PASSWORD);

        final var response = client
                .target(apiRoot + "/user/password/reset/complete")
                .request(APPLICATION_JSON)
                .post(Entity.entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);
    }

    /**
     * Missing token field → 400 (validation failure).
     */
    @Test
    public void completeReset_missingToken_returns400() {
        final var body = new CompletePasswordResetRequest();
        body.setPassword(NEW_PASSWORD);

        final var response = client
                .target(apiRoot + "/user/password/reset/complete")
                .request(APPLICATION_JSON)
                .post(Entity.entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);
    }

    /**
     * Missing password field → 400 (validation failure).
     */
    @Test
    public void completeReset_missingPassword_returns400() {
        final var body = new CompletePasswordResetRequest();
        body.setToken("some-token-value");

        final var response = client
                .target(apiRoot + "/user/password/reset/complete")
                .request(APPLICATION_JSON)
                .post(Entity.entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);
    }

    /**
     * A token may only be used once; a second call with the same token returns 400.
     *
     * <p>Must run before {@link #completeReset_validToken_returns200} since both change the
     * user's password and thus invalidate sessions.
     */
    @Test(dependsOnMethods = {
            "requestReset_unknownEmail_returns204",
            "requestReset_missingEmail_returns400",
            "requestReset_malformedEmail_returns400",
            "requestReset_noAuthRequired",
            "completeReset_unknownToken_returns400",
            "completeReset_missingToken_returns400",
            "completeReset_missingPassword_returns400"
    })
    public void completeReset_tokenSingleUse_secondCallReturns400() {
        final var user = clientContext.getUser();
        final var expiry = new Timestamp(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        final var token = tokenDao.createToken(user, expiry);

        final var body = new CompletePasswordResetRequest();
        body.setToken(token);
        body.setPassword(NEW_PASSWORD + "-single-use");

        // First call succeeds
        final var first = client
                .target(apiRoot + "/user/password/reset/complete")
                .request(APPLICATION_JSON)
                .post(Entity.entity(body, APPLICATION_JSON));
        assertEquals(first.getStatus(), 204);

        // Second call with the same token must fail (token consumed)
        final var second = client
                .target(apiRoot + "/user/password/reset/complete")
                .request(APPLICATION_JSON)
                .post(Entity.entity(body, APPLICATION_JSON));
        assertEquals(second.getStatus(), 400);
    }

    /**
     * Valid token → 204; password is updated.
     * Must run after all non-destructive tests since it changes the user's passwordHash
     * and invalidates all existing sessions.
     */
    @Test(dependsOnMethods = "completeReset_tokenSingleUse_secondCallReturns400")
    public void completeReset_validToken_returns204() {
        final var user = clientContext.getUser();
        final var expiry = new Timestamp(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        final var token = tokenDao.createToken(user, expiry);

        final var body = new CompletePasswordResetRequest();
        body.setToken(token);
        body.setPassword(NEW_PASSWORD);

        final var response = client
                .target(apiRoot + "/user/password/reset/complete")
                .request(APPLICATION_JSON)
                .post(Entity.entity(body, APPLICATION_JSON));

        assertEquals(response.getStatus(), 204);
    }

    /**
     * After a successful reset, all pre-existing sessions are invalidated because the
     * passwordHash (from which session secrets are derived) has changed.
     */
    @Test(dependsOnMethods = "completeReset_validToken_returns204")
    public void completeReset_oldSessionInvalidatedAfterReset() {
        final Response response = client
                .target(apiRoot + "/user/me")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, sessionSecretBeforeReset)
                .get();

        assertEquals(response.getStatus(), 403);
    }

}

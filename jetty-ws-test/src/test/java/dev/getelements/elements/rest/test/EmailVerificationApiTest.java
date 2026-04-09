package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.dao.UidVerificationTokenDao;
import dev.getelements.elements.sdk.model.user.EmailVerificationRequest;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;

/**
 * Integration tests for the email verification REST endpoints.
 *
 * <p>Tests {@code GET /verify?token=...} (token consumption) without needing a live SMTP server.
 * Token setup is done directly via {@link UidVerificationTokenDao} to keep the tests self-contained.
 *
 * <p>Tests for {@code POST /user/me/email/verify} cover access control only; the full email-sending
 * path is covered by {@code EmailVerificationServiceTest} unit tests.
 */
public class EmailVerificationApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[]{TestUtils.getInstance().getTestFixture(EmailVerificationApiTest.class)};
    }

    @Inject @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    @Inject
    private UidVerificationTokenDao tokenDao;

    @BeforeClass
    public void setup() {
        clientContext.createUser("email-verification-api-test")
                     .createSession();
    }

    // ---------- GET /verify (complete verification) ----------

    /**
     * A valid token → 200 with verificationStatus VERIFIED.
     */
    @Test
    public void completeVerification_validToken_returnsVerified() {
        final var user = clientContext.getUser();
        final var email = user.getEmail();
        final var expiry = new Timestamp(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        final var token = tokenDao.createToken(user, SCHEME_EMAIL, email, expiry);

        final var uid = client
                .target(apiRoot + "/verify")
                .queryParam("token", token)
                .request(APPLICATION_JSON)
                .get(UserUid.class);

        assertEquals(uid.getVerificationStatus(), VerificationStatus.VERIFIED);
    }

    /**
     * An unknown token → 404.
     */
    @Test
    public void completeVerification_unknownToken_returns404() {
        final var response = client
                .target(apiRoot + "/verify")
                .queryParam("token", "no-such-token-at-all")
                .request(APPLICATION_JSON)
                .get();

        assertEquals(response.getStatus(), 404);
    }

    /**
     * A token may only be used once; a second call returns 404.
     */
    @Test
    public void completeVerification_tokenSingleUse_secondCallReturns404() {
        final var user = clientContext.getUser();
        final var email = user.getEmail();
        final var expiry = new Timestamp(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        final var token = tokenDao.createToken(user, SCHEME_EMAIL, email, expiry);

        // First call succeeds
        final var first = client
                .target(apiRoot + "/verify")
                .queryParam("token", token)
                .request(APPLICATION_JSON)
                .get();
        assertEquals(first.getStatus(), 200);

        // Second call must fail with 404 (token consumed)
        final var second = client
                .target(apiRoot + "/verify")
                .queryParam("token", token)
                .request(APPLICATION_JSON)
                .get();
        assertEquals(second.getStatus(), 404);
    }

    // ---------- POST /user/me/email/verify (request verification) ----------

    /**
     * Unauthenticated request → 403 (anonymous callers are forbidden).
     */
    @Test
    public void requestVerification_unauthenticated_returns403() {
        final var request = new EmailVerificationRequest();
        request.setEmail(clientContext.getUser().getEmail());

        final Response response = client
                .target(apiRoot + "/user/me/email/verify")
                .request(APPLICATION_JSON)
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);
    }

    /**
     * Authenticated request with a fresh (never-verified) email but no SMTP configured → 400
     * (InvalidDataException).  We use a freshly-generated address rather than the user's main email
     * because other tests in this class may already have set the main email to VERIFIED (which would
     * trigger an early-return 200 instead of reaching the SMTP path).
     */
    @Test
    public void requestVerification_authenticated_withoutSmtp_returns400() {
        final var freshEmail = "smtp-test-" + UUID.randomUUID() + "@test.example.com";
        final var request = new EmailVerificationRequest();
        request.setEmail(freshEmail);

        final Response response = client
                .target(apiRoot + "/user/me/email/verify")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);
    }

}

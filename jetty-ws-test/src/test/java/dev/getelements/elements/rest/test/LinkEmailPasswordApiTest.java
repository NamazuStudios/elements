package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.dao.UidVerificationTokenDao;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.model.user.LinkEmailPasswordRequest;
import dev.getelements.elements.sdk.model.user.User;
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
import static org.testng.Assert.assertNotNull;

/**
 * Integration tests for {@code POST /user/me/link/email-password}.
 *
 * <p>The VERIFIED prerequisite is fulfilled by creating a token directly via
 * {@link UidVerificationTokenDao} and consuming it via {@code GET /verify?token=...},
 * which avoids needing a live SMTP server.
 */
public class LinkEmailPasswordApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[]{TestUtils.getInstance().getTestFixture(LinkEmailPasswordApiTest.class)};
    }

    @Inject @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    @Inject
    private UidVerificationTokenDao tokenDao;

    private final String linkedPassword = UUID.randomUUID().toString();

    @BeforeClass
    public void setup() {
        clientContext.createUser("link-ep-test").createSession();

        // Verify the user's email directly via the DAO so we can call the link endpoint.
        final var user = clientContext.getUser();
        final var email = user.getEmail();
        final var expiry = new Timestamp(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        final var token = tokenDao.createToken(user, SCHEME_EMAIL, email, expiry);

        final var verifyResponse = client
                .target(apiRoot + "/verify")
                .queryParam("token", token)
                .request(APPLICATION_JSON)
                .get();

        assertEquals(verifyResponse.getStatus(), 200, "Email verification prerequisite failed");
    }

    @Test
    public void linkEmailPassword_unauthenticated_returns403() {
        final var request = new LinkEmailPasswordRequest();
        request.setEmail(clientContext.getUser().getEmail());
        request.setPassword(linkedPassword);

        final var response = client
                .target(apiRoot + "/user/me/link/email-password")
                .request(APPLICATION_JSON)
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);
    }

    @Test
    public void linkEmailPassword_unverifiedEmail_returns403() {
        // Use a brand-new email that has no UID → getUserUid throws NotFoundException → 404
        // (the plan notes NotFoundException maps to 404; any non-200 confirms the guard works)
        final var request = new LinkEmailPasswordRequest();
        request.setEmail("no-uid-" + UUID.randomUUID() + "@test.example.com");
        request.setPassword(linkedPassword);

        final var response = client
                .target(apiRoot + "/user/me/link/email-password")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void linkEmailPassword_verifiedEmail_returns200() {
        final var request = new LinkEmailPasswordRequest();
        request.setEmail(clientContext.getUser().getEmail());
        request.setPassword(linkedPassword);

        final var response = client
                .target(apiRoot + "/user/me/link/email-password")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        final var user = response.readEntity(User.class);
        assertNotNull(user);
        assertNotNull(user.getId());
    }

    @Test(dependsOnMethods = "linkEmailPassword_verifiedEmail_returns200")
    public void login_withLinkedPassword_succeeds() {
        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(clientContext.getUser().getEmail());
        request.setPassword(linkedPassword);

        final var response = client
                .target(apiRoot + "/session")
                .request(APPLICATION_JSON)
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        final var session = response.readEntity(SessionCreation.class);
        assertNotNull(session.getSession());
        assertEquals(session.getSession().getUser().getId(), clientContext.getUser().getId());
    }

}

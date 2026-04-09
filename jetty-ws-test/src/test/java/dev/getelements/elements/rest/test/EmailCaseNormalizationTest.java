package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserCreateRequest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Verifies that email addresses are case-normalised (stored as lowercase) on signup
 * and that login succeeds regardless of the case the caller uses.
 */
public class EmailCaseNormalizationTest {

    @Factory
    public Object[] getTests() {
        return new Object[]{TestUtils.getInstance().getTestFixture(EmailCaseNormalizationTest.class)};
    }

    @Inject @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    // Register with a MIXED-CASE email to verify normalisation at creation time.
    private final String rawEmail    = "Test.User-" + UUID.randomUUID() + "@Example.COM";
    private final String lowerEmail  = rawEmail.toLowerCase();
    private final String name        = "email-norm-" + UUID.randomUUID();
    private final String password    = UUID.randomUUID().toString();

    private String userId;

    @Test
    public void signup_mixedCaseEmail_storedAsLowercase() {
        final var request = new UserCreateRequest();
        request.setName(name);
        request.setEmail(rawEmail);
        request.setPassword(password);

        final var user = client
                .target(apiRoot + "/signup/session")
                .request(APPLICATION_JSON)
                .post(Entity.entity(request, APPLICATION_JSON), SessionCreation.class);

        assertNotNull(user);
        assertNotNull(user.getSession());

        final var createdUser = user.getSession().getUser();
        assertNotNull(createdUser);
        userId = createdUser.getId();

        assertEquals(createdUser.getEmail(), lowerEmail,
                "Email should be stored as lowercase regardless of how it was supplied");
    }

    @Test(dependsOnMethods = "signup_mixedCaseEmail_storedAsLowercase")
    public void login_mixedCaseEmail_succeeds() {
        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(rawEmail);   // original mixed-case
        request.setPassword(password);

        final var response = client
                .target(apiRoot + "/session")
                .request(APPLICATION_JSON)
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200, "Login with mixed-case email should succeed");
        final var session = response.readEntity(SessionCreation.class);
        assertEquals(session.getSession().getUser().getId(), userId);
    }

    @Test(dependsOnMethods = "signup_mixedCaseEmail_storedAsLowercase")
    public void login_uppercaseEmail_succeeds() {
        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(rawEmail.toUpperCase());
        request.setPassword(password);

        final var response = client
                .target(apiRoot + "/session")
                .request(APPLICATION_JSON)
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200, "Login with all-uppercase email should succeed");
        final var session = response.readEntity(SessionCreation.class);
        assertEquals(session.getSession().getUser().getId(), userId);
    }

    @Test(dependsOnMethods = "signup_mixedCaseEmail_storedAsLowercase")
    public void login_lowercaseEmail_succeeds() {
        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(lowerEmail);
        request.setPassword(password);

        final var response = client
                .target(apiRoot + "/session")
                .request(APPLICATION_JSON)
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200, "Login with lowercase email should succeed");
        final var session = response.readEntity(SessionCreation.class);
        assertEquals(session.getSession().getUser().getId(), userId);
    }

}

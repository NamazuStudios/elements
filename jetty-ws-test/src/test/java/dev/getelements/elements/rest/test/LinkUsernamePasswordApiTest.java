package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.model.user.LinkUsernamePasswordRequest;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Integration tests for {@code POST /user/me/link/username-password}.
 */
public class LinkUsernamePasswordApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[]{TestUtils.getInstance().getTestFixture(LinkUsernamePasswordApiTest.class)};
    }

    @Inject @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    private final String linkedPassword = UUID.randomUUID().toString();

    @BeforeClass
    public void setup() {
        clientContext.createUser("link-up-test").createSession();
    }

    @Test
    public void linkUsernamePassword_unauthenticated_returns403() {
        final var request = new LinkUsernamePasswordRequest();
        request.setUsername(clientContext.getUser().getName());
        request.setPassword(linkedPassword);

        final var response = client
                .target(apiRoot + "/user/me/link/username-password")
                .request(APPLICATION_JSON)
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);
    }

    @Test
    public void linkUsernamePassword_mismatchedName_returns403() {
        final var request = new LinkUsernamePasswordRequest();
        request.setUsername("definitely-not-" + clientContext.getUser().getName());
        request.setPassword(linkedPassword);

        final var response = client
                .target(apiRoot + "/user/me/link/username-password")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);
    }

    @Test
    public void linkUsernamePassword_matchingName_returns200() {
        final var request = new LinkUsernamePasswordRequest();
        request.setUsername(clientContext.getUser().getName());
        request.setPassword(linkedPassword);

        final var response = client
                .target(apiRoot + "/user/me/link/username-password")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200);
        final var user = response.readEntity(User.class);
        assertNotNull(user);
        assertNotNull(user.getId());
    }

    @Test(dependsOnMethods = "linkUsernamePassword_matchingName_returns200")
    public void login_withLinkedPassword_succeeds() {
        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(clientContext.getUser().getName());
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

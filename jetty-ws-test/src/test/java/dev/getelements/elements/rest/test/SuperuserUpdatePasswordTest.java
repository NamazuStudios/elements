package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUpdatePasswordRequest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Regression coverage for the superuser self-service password change (#183): the admin dashboard is
 * superuser-only, and its change-password card hits {@code PUT /user/{id}/password} — which previously
 * threw an unmapped "Not implemented" error and surfaced as a 500 for the only users who could reach
 * the card. Also pins the promised credential rotation: every outstanding session is revoked by the
 * change, and the returned fresh secret keeps the caller signed in.
 */
public class SuperuserUpdatePasswordTest {

    public static final String ORIGINAL_PASSWORD = ClientContext.DUMMY_PASSWORD;

    public static final String UPDATED_PASSWORD = "superuser_password_update";

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(SuperuserUpdatePasswordTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superuser0;

    private String otherSessionSecret;

    @BeforeClass
    public void setUp() {

        superuser0.createSuperuser("superuser-password-test")
                .createSession();

        // A second concurrent session for the same account: the password change must revoke it, as
        // promised by the dashboard copy ("your other sessions will be signed out").

        superuser0.createSession();
        otherSessionSecret = superuser0.getSessionSecret();

    }

    @Test
    public void testSuperuserUpdatesOwnPassword() {

        final var request = new UserUpdatePasswordRequest();
        request.setNewPassword(UPDATED_PASSWORD);
        request.setOldPassword(ORIGINAL_PASSWORD);

        var response = client
                .target(format("%s/user/%s/password", apiRoot, superuser0.getUser().getId()))
                .request()
                .header("Authorization", format("Bearer %s", superuser0.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        // The fresh session is issued after the revocation: the caller stays signed in under the
        // rotated credential, exactly as the dashboard card expects.

        final var sessionCreation = response.readEntity(SessionCreation.class);
        assertNotNull(sessionCreation.getSessionSecret());
        assertNotNull(sessionCreation.getSession());
        assertEquals(sessionCreation.getSession().getUser().getLevel(), User.Level.SUPERUSER);

        response = client
                .target(format("%s/user/me", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", sessionCreation.getSessionSecret()))
                .get();

        assertEquals(200, response.getStatus());
        assertEquals(response.readEntity(User.class).getId(), superuser0.getUser().getId());

    }

    @Test(dependsOnMethods = "testSuperuserUpdatesOwnPassword")
    public void testOtherSessionsRevokedByTheChange() {

        // The pre-change session must no longer authenticate -- the exact promise the change-password
        // card makes. Before the fix, signed session tokens survived the change unrevoked.

        final var response = client
                .target(format("%s/user/me", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", otherSessionSecret))
                .get();

        assertEquals(401, response.getStatus());
        assertNotEquals(otherSessionSecret, null);

    }

    @Test(dependsOnMethods = "testSuperuserUpdatesOwnPassword")
    public void testNewPasswordAuthenticates() {

        final var request = new dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest();
        request.setUserId(superuser0.getUser().getName());
        request.setPassword(UPDATED_PASSWORD);

        final var response = client
                .target(format("%s/session", apiRoot))
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());
        assertNotNull(response.readEntity(SessionCreation.class).getSessionSecret());

    }

}

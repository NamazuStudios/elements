package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import static dev.getelements.elements.rest.test.ClientContext.DUMMY_PASSWORD;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static org.testng.Assert.*;

public class AnonymousSignInTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(AnonymousSignInTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext userClientContext;

    @Inject
    private ClientContext otherUserClientContext;

    @BeforeClass
    public void setUp() {

        userClientContext
                .createUser("sign-in")
                .createProfile("SignIn");

        otherUserClientContext
                .createUser("sign-in")
                .createProfile("SignInOther");

    }

    @DataProvider
    public Object[][] userNameAndPassword() {

        final var user = userClientContext.getUser();

        return new Object[][] {
                new Object[] { user.getId(), DUMMY_PASSWORD },
                new Object[] { user.getName(), DUMMY_PASSWORD },
                new Object[] { user.getEmail(), DUMMY_PASSWORD }
        };

    }

    @Test(dataProvider = "userNameAndPassword")
    public void testSignInNoProfile(final String username, final String password) {

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(username);
        request.setPassword(password);

        final var response = client
                .target(apiRoot + "/session")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final var status = response.getStatus();
        assertEquals(status, 200);

        final var session = response.readEntity(SessionCreation.class);
        assertNotNull(session);
        assertNotNull(session.getSession());
        assertNotNull(session.getSessionSecret());
        assertEquals(session.getSession().getUser().getId(), userClientContext.getUser().getId());
        assertNull(session.getSession().getProfile());

    }

    @Test(dataProvider = "userNameAndPassword")
    public void testSignInProfileId(final String username, final String password) {

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(username);
        request.setPassword(password);
        request.setProfileId(userClientContext.getDefaultProfile().getId());

        final var response = client
                .target(apiRoot + "/session")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final var status = response.getStatus();
        assertEquals(status, 200);

        final var session = response.readEntity(SessionCreation.class);
        assertNotNull(session);
        assertNotNull(session.getSession());
        assertNotNull(session.getSessionSecret());
        assertEquals(session.getSession().getUser().getId(), userClientContext.getUser().getId());
        assertEquals(session.getSession().getProfile().getId(), userClientContext.getDefaultProfile().getId());

    }

    @Test(dataProvider = "userNameAndPassword")
    public void testSignInProfileSelector(final String username, final String password) {

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(username);
        request.setPassword(password);
        request.setProfileSelector(format("displayName:%s", userClientContext.getDefaultProfile().getDisplayName()));

        final var response = client
                .target(apiRoot + "/session")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final var status = response.getStatus();
        assertEquals(status, 200);

        final var session = response.readEntity(SessionCreation.class);
        assertNotNull(session);
        assertNotNull(session.getSession());
        assertNotNull(session.getSessionSecret());
        assertEquals(session.getSession().getUser().getId(), userClientContext.getUser().getId());
        assertEquals(session.getSession().getProfile().getId(), userClientContext.getDefaultProfile().getId());

    }

    @DataProvider
    public Object[][] userNameAndPasswordBadProfile() {

        final var user = userClientContext.getUser();

        return new Object[][] {
                new Object[] { user.getId(), DUMMY_PASSWORD, otherUserClientContext.getDefaultProfile() },
                new Object[] { user.getName(), DUMMY_PASSWORD, otherUserClientContext.getDefaultProfile() },
                new Object[] { user.getEmail(), DUMMY_PASSWORD, otherUserClientContext.getDefaultProfile() }
        };

    }



    @Test(dataProvider = "userNameAndPasswordBadProfile")
    public void testBadSignInProfileId(final String username, final String password, final Profile other) {

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(username);
        request.setPassword(password);
        request.setProfileId(other.getId());

        final var response = client
                .target(apiRoot + "/session")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final var status = response.getStatus();
        assertEquals(status, 403);

    }

    @Test(dataProvider = "userNameAndPasswordBadProfile")
    public void testBadSignInProfileSelector(final String username, final String password, final Profile other) {

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(username);
        request.setPassword(password);
        request.setProfileSelector(format("displayName:%s", other.getDisplayName()));

        final var response = client
                .target(apiRoot + "/session")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final var status = response.getStatus();
        assertEquals(status, 404);

    }

}

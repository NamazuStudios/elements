package dev.getelements.elements.rest;

import dev.getelements.elements.model.profile.CreateProfileRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.user.UserCreateRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.util.HashMap;

import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.Headers.SOCIALENGINE_SESSION_SECRET;
import static dev.getelements.elements.rest.TestUtils.*;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class CreateUserAndProfileTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(CreateUserAndProfileTest.class),
            TestUtils.getInstance().getUnixFSTest(CreateUserAndProfileTest.class)
        };
    }

    private User user;

    private Profile profile;

    private SessionCreation sessionCreation;

    private final String name = "testuser-name-" + randomUUID().toString();

    private final String email = "testuser-email-" + randomUUID().toString() + "@example.com";

    private final String password = randomUUID().toString();

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
                new Object[] { SESSION_SECRET },
                new Object[] { SOCIALENGINE_SESSION_SECRET }
        };
    }

    @Test
    public void createUser() {

        final var toCreate = new UserCreateRequest();
        toCreate.setName(name);
        toCreate.setEmail(email);
        toCreate.setPassword(password);

        user = client
            .target(apiRoot + "/user")
            .request()
            .post(Entity.entity(toCreate, APPLICATION_JSON))
            .readEntity(User.class);

        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getEmail());
        assertEquals(user.getLevel(), User.Level.USER);
        assertNull(user.getFacebookId());

    }

    @DataProvider
    public Object[][] credentialsProvider() {
        return new Object[][] {
            new Object[]{name, password},
            new Object[]{email, password},
            new Object[]{user.getId(), password}
        };
    }

    @Test(dependsOnMethods = "createUser", dataProvider = "credentialsProvider")
    public void testUserLogin(final String uid, final String password) {

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(uid);
        request.setPassword(password);

        final Response response = client
            .target(apiRoot + "/session")
            .request()
            .post(Entity.entity(request, APPLICATION_JSON));

        sessionCreation = response.readEntity(SessionCreation.class);

        assertEquals(response.getStatus(), 200);
        assertNotNull(sessionCreation);
        assertNotNull(sessionCreation.getSession());
        assertNotNull(sessionCreation.getSessionSecret());

        final Session session = sessionCreation.getSession();
        assertEquals(session.getUser(), user);
        assertEquals(session.getProfile(), profile);

    }

    @Test(dependsOnMethods = "createUser", dataProvider = "credentialsProvider")
    public void testBogusUserLogin(final String uid, final String _ignored) {

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(uid);
        request.setPassword("bogus password");

        final Response response = client
            .target(apiRoot + "/session")
            .request()
            .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(dependsOnMethods = "testUserLogin")
    public void createProfileExpectingFailureNoAuth() {
        final var toCreate = new CreateProfileRequest();

        toCreate.setUserId(user.getId());
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplicationId(clientContext.getApplication().getId());

        final Response response = client
            .target(apiRoot + "/profile")
            .request()
            .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(dependsOnMethods = "testUserLogin", dataProvider = "getAuthHeader")
    public void createForUserHappy(final String authHeader) {

        final var toCreate = new CreateProfileRequest();

        toCreate.setUserId(user.getId());
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplicationId(clientContext.getApplication().getId());

        final var metadata = new HashMap<String, Object>();
        metadata.put("foo", "bar");

        toCreate.setMetadata(metadata);

        final Response response = client
            .target(apiRoot + "/profile")
            .request()
            .header(authHeader, sessionCreation.getSessionSecret())
            .post(Entity.entity(toCreate, APPLICATION_JSON));

        profile = response.readEntity(Profile.class);

        assertNull(profile.getMetadata());
        assertNotNull(profile.getId());
        assertEquals(profile.getUser(), user);
        assertEquals(profile.getApplication(), clientContext.getApplication());

    }

    @Test(dependsOnMethods = "testUserLogin", dataProvider = "getAuthHeader")
    public void createForBogusUser(final String authHeader) {

        final var toCreate = new CreateProfileRequest();

        // We want to test that the system will reject the bogus user

        toCreate.setUserId(clientContext.getUser() == null ? "bogusId" : clientContext.getUser().getId());
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplicationId(clientContext.getApplication().getId());

        final Response response = client
                .target(apiRoot + "/profile")
                .request()
                .header(authHeader, sessionCreation.getSessionSecret())
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);

    }

}

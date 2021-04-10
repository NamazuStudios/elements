package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.session.UsernamePasswordSessionRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static com.namazustudios.socialengine.security.AuthorizationHeader.AUTH_HEADER;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class SignupUserTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(SignupUserTest.class),
            TestUtils.getInstance().getUnixFSTest(SignupUserTest.class)
        };
    }

    private User user;

    private Profile profile;

    private SessionCreation sessionCreation;

    private final String name = "testuser-" + randomUUID().toString();

    private final String email = "testuser-" + randomUUID().toString() + "@example.com";

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
            new Object[] { AUTH_HEADER, "%s" },
            new Object[] { AUTH_HEADER, "Bearer %s" },
            new Object[] { SESSION_SECRET, "%s" },
            new Object[] { SOCIALENGINE_SESSION_SECRET, "%s" }
        };
    }

    @Test
    public void createUser() {

        final UserCreateRequest toCreate = new UserCreateRequest();
        toCreate.setName(name);
        toCreate.setEmail(email);
        toCreate.setPassword(password);

        user = client
                .target(apiRoot + "/signup")
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

        final UsernamePasswordSessionRequest request = new UsernamePasswordSessionRequest();
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

    @Test(dependsOnMethods = "testUserLogin")
    public void createProfileExpectingFailureNoAuth() {
        final CreateProfileRequest toCreate = new CreateProfileRequest();

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
    public void createForUserHappy(final String authHeader, final String authHeaderFormat) {

        final var toCreate = new CreateProfileRequest();
        final var authHeaderValue = format(authHeaderFormat, sessionCreation.getSessionSecret());

        toCreate.setUserId(user.getId());
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplicationId(clientContext.getApplication().getId());

        final var response = client
            .target(apiRoot + "/profile")
            .request()
            .header(authHeader, authHeaderValue)
            .post(Entity.entity(toCreate, APPLICATION_JSON));

        profile = response.readEntity(Profile.class);

        assertNotNull(profile.getId());
        assertEquals(profile.getUser(), user);
        assertEquals(profile.getApplication(), clientContext.getApplication());

    }

    @Test(dependsOnMethods = "testUserLogin", dataProvider = "getAuthHeader")
    public void createForBogusUser(final String authHeader, final String authHeaderFormat) {

        final var toCreate = new CreateProfileRequest();
        final var authHeaderValue = format(authHeaderFormat, sessionCreation.getSessionSecret());

        // We want to test that the system will reject the bogus user

        toCreate.setUserId(clientContext.getUser() == null ? "bogusId" : clientContext.getUser().getId());
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplicationId(clientContext.getApplication().getId());

        final var response = client
            .target(apiRoot + "/profile")
            .request()
            .header(authHeader, authHeaderValue)
            .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);

    }

}

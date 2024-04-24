package dev.getelements.elements.rest.test;

import dev.getelements.elements.model.profile.CreateProfileRequest;
import dev.getelements.elements.model.profile.CreateProfileSignupRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.user.UserCreateRequest;
import dev.getelements.elements.model.user.UserCreateResponse;
import dev.getelements.elements.rest.test.ClientContext;
import dev.getelements.elements.rest.test.TestUtils;
import org.dozer.DozerBeanMapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.Headers.SOCIALENGINE_SESSION_SECRET;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.security.AuthorizationHeader.AUTH_HEADER;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

public class AnonymousSignupTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(AnonymousSignupTest.class),
            TestUtils.getInstance().getUnixFSTest(AnonymousSignupTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    private String name = "testuser-" + randomUUID().toString();

    private String email = "testuser-" + randomUUID().toString() + "@example.com";

    private String password = randomUUID().toString();

    private User user;

    private Profile profile;

    private SessionCreation sessionCreation;

    @Test
    public void createUser() {

        final var toCreate = new UserCreateRequest();
        final var createProfileSignupRequest = new CreateProfileSignupRequest();
        createProfileSignupRequest.setApplicationId(clientContext.getApplication().getId());

        toCreate.setProfiles(singletonList(createProfileSignupRequest));

        final var response = client
                .target(apiRoot + "/signup")
                .request()
                .post(entity(toCreate, APPLICATION_JSON))
                .readEntity(UserCreateResponse.class);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getEmail());
        assertEquals(response.getLevel(), User.Level.USER);
        assertNull(response.getFacebookId());
        assertNotNull(response.getPassword());

        assertEquals(response.getProfiles().size(), 1);

        profile = response.getProfiles().get(0);
        assertNotNull(profile.getDisplayName());
        assertEquals(profile.getApplication().getId(), createProfileSignupRequest.getApplicationId());

        name = response.getName();
        email = response.getEmail();
        password = response.getPassword();

        final var mapper = new DozerBeanMapper();
        user = mapper.map(response, User.class);

    }


    @Test(dependsOnMethods = "createUser", dataProvider = "credentialsProvider")
    public void testUserLogin(final String uid, final String password) {

        final UsernamePasswordSessionRequest request = new UsernamePasswordSessionRequest();
        request.setUserId(uid);
        request.setPassword(password);
        request.setProfileId(profile.getId());

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
        assertEquals(session.getUser().getId(), user.getId());
        assertEquals(session.getProfile().getId(), profile.getId());

    }

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
                new Object[] { AUTH_HEADER, "%s" },
                new Object[] { AUTH_HEADER, "Bearer %s" },
                new Object[] { SESSION_SECRET, "%s" },
                new Object[] { SOCIALENGINE_SESSION_SECRET, "%s" }
        };
    }

    @DataProvider
    public Object[][] credentialsProvider() {
        return new Object[][] {
                new Object[]{name, password},
                new Object[]{email, password},
                new Object[]{user.getId(), password}
        };
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

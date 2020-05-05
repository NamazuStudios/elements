package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.session.UsernamePasswordSessionRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

@Guice(modules = {EmbeddedRestApiIntegrationTestModule.class})
public class CreateUserAndProfileTest {

    private User user;

    private Profile profile;

    private SessionCreation sessionCreation;

    private final String uid = "testuser-" + randomUUID().toString();

    private final String email = "testuser-" + randomUUID().toString() + "@example.com";

    private final String password = randomUUID().toString();

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    @Test
    public void createUser() {

        final UserCreateRequest toCreate = new UserCreateRequest();
        toCreate.setName(uid);
        toCreate.setEmail(email);
        toCreate.setPassword(password);

        user = client
            .target("http://localhost:8081/api/rest/user")
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
            new Object[]{uid, password},
            new Object[]{email, password},
            new Object[]{user.getId(), password}
        };
    }

    @Test(dependsOnMethods = "createUser", dataProvider = "credentialsProvider")
    public void loginUserWithEmail(final String uid, final String password) {

        final UsernamePasswordSessionRequest request = new UsernamePasswordSessionRequest();
        request.setUserId(uid);
        request.setPassword(password);

        sessionCreation = client
            .target("http://localhost:8081/api/rest/session")
            .request()
            .post(Entity.entity(request, APPLICATION_JSON))
            .readEntity(SessionCreation.class);

        assertNotNull(sessionCreation);
        assertNotNull(sessionCreation.getSession());
        assertNotNull(sessionCreation.getSessionSecret());

        final Session session = sessionCreation.getSession();
        assertEquals(session.getUser(), user);
        assertEquals(session.getProfile(), profile);

    }

    @Test(dependsOnMethods = "loginUserWithEmail")
    public void createProfileExpectingFailureNoAuth() {
        final Profile toCreate = new Profile();

        toCreate.setUser(user);
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplication(clientContext.getApplication());

        final Response response = client
            .target("http://localhost:8081/api/rest/profile")
            .request()
            .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(dependsOnMethods = "loginUserWithEmail")
    public void createForUserHappy() {

        final Profile toCreate = new Profile();

        toCreate.setUser(user);
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplication(clientContext.getApplication());

        final Response response = client
            .target("http://localhost:8081/api/rest/profile")
            .request()
            .header("Elements-SessionSecret", sessionCreation.getSessionSecret())
            .post(Entity.entity(toCreate, APPLICATION_JSON));

        profile = response.readEntity(Profile.class);

        assertNotNull(profile.getId());
        assertEquals(profile.getUser(), user);
        assertEquals(profile.getApplication(), clientContext.getApplication());

    }

    @Test(dependsOnMethods = "loginUserWithEmail")
    public void createForBogusUser() {

        final Profile toCreate = new Profile();

        // We want to test that the system will reject the bogus user

        toCreate.setUser(clientContext.getUser());
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplication(clientContext.getApplication());

        final Response response = client
                .target("http://localhost:8081/api/rest/profile")
                .request()
                .header("Elements-SessionSecret", sessionCreation.getSessionSecret())
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);

    }


}

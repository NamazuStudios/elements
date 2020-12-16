package com.namazustudios.socialengine.rest;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.appnode.guice.JaxRSClientModule;
import com.namazustudios.socialengine.appnode.guice.ServicesModule;
import com.namazustudios.socialengine.appnode.guice.VersionModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.session.UsernamePasswordSessionRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import com.namazustudios.socialengine.rt.lua.guice.JeroMQEmbeddedTestService;
import com.namazustudios.socialengine.rt.lua.guice.LuaManifestLoaderTest;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.lua.guice.TestJavaEvent;
import com.namazustudios.socialengine.rt.xodus.XodusContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import com.namazustudios.socialengine.service.guice.GuiceStandardNotificationFactoryModule;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import org.apache.bval.guice.ValidationModule;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MINUTES;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

@Guice(modules = {EmbeddedRestApiIntegrationTestModule.class})
public class CreateUserAndProfileTest {

    private User user;

    private Profile profile;

    private SessionCreation sessionCreation;

    private final String name = "testuser-name-" + randomUUID().toString();

    private final String email = "testuser-email-" + randomUUID().toString() + "@example.com";

    private static final String NODE_ADDRESS = "tcp://localhost:45002";

    private final String password = randomUUID().toString();

    final DefaultConfigurationSupplier defaultConfigurationSupplier = new DefaultConfigurationSupplier();

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
            .withNodeModule(new ConfigurationModule(defaultConfigurationSupplier))
            .withNodeModule(new LuaModule())
            .withNodeModule(new MongoDaoModule())
            .withNodeModule(new MongoCoreModule())
            .withNodeModule(new ValidationModule())
            .withNodeModule(new MongoSearchModule())
            .withNodeModule(new ServicesModule())
            .withNodeModule(new XodusContextModule()
                    .withSchedulerThreads(1)
                    .withHandlerTimeout(3, MINUTES))
            .withNodeModule(new XodusEnvironmentModule()
                    .withTempEnvironments())
            .withNodeModule(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(TestJavaEvent.class).toInstance(mock(TestJavaEvent.class));
                }
            })
            .withDefaultHttpClient()
            .withNodeAddress(NODE_ADDRESS)
            .start();

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

        final UserCreateRequest toCreate = new UserCreateRequest();
        toCreate.setName(name);
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
            .target("http://localhost:8081/api/rest/session")
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

        final UsernamePasswordSessionRequest request = new UsernamePasswordSessionRequest();
        request.setUserId(uid);
        request.setPassword("bogus password");

        final Response response = client
            .target("http://localhost:8081/api/rest/session")
            .request()
            .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 403);

    }

    @Test(dependsOnMethods = "testUserLogin")
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

    @Test(dependsOnMethods = "testUserLogin", dataProvider = "getAuthHeader")
    public void createForUserHappy(final String authHeader) {

        final Profile toCreate = new Profile();

        toCreate.setUser(user);
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplication(clientContext.getApplication());

        final Response response = client
            .target("http://localhost:8081/api/rest/profile")
            .request()
            .header(authHeader, sessionCreation.getSessionSecret())
            .post(Entity.entity(toCreate, APPLICATION_JSON));

        profile = response.readEntity(Profile.class);

        assertNotNull(profile.getId());
        assertEquals(profile.getUser(), user);
        assertEquals(profile.getApplication(), clientContext.getApplication());

    }

    @Test(dependsOnMethods = "testUserLogin", dataProvider = "getAuthHeader")
    public void createForBogusUser(final String authHeader) {

        final Profile toCreate = new Profile();

        // We want to test that the system will reject the bogus user

        toCreate.setUser(clientContext.getUser());
        toCreate.setDisplayName("Paddy O' Furniture");
        toCreate.setApplication(clientContext.getApplication());

        final Response response = client
                .target("http://localhost:8081/api/rest/profile")
                .request()
                .header(authHeader, sessionCreation.getSessionSecret())
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        assertEquals(response.getStatus(), 400);

    }


}

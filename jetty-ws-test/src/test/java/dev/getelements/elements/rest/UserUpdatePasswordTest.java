package dev.getelements.elements.rest;

import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.user.UserUpdatePasswordRequest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

public class UserUpdatePasswordTest {

    public static final String ORIGINAL_PASSWORD = ClientContext.DUMMY_PASSWORD;

    public static final String FIRST_UPDATE = "first_password_update";

    public static final String SECOND_UPDATE = "second_password_update";

    public static final String ALWAYS_FAILS = "always_fails_every_time";

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(UserUpdatePasswordTest.class),
                TestUtils.getInstance().getUnixFSTest(UserUpdatePasswordTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user0;

    @Inject
    private ClientContext user1;

    @BeforeClass
    public void setUp() {

        user0.createUser("password-test")
                .createProfile("password-test")
                .createSession();

        user1.createUser("password-test")
                .createProfile("password-test")
                .createSession();

    }

    @DataProvider
    public Object[][] getContexts() {
        return new Object[][] { {user0}, {user1} };
    }

    @Test(dataProvider = "getContexts")
    public void testUpdatePassword(final ClientContext context) {

        final var request = new UserUpdatePasswordRequest();
        request.setNewPassword(FIRST_UPDATE);
        request.setOldPassword(ORIGINAL_PASSWORD);

        var response = client
                .target(format("%s/user/%s/password", apiRoot, context.getUser().getId()))
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        final var sessionCreation = response.readEntity(SessionCreation.class);
        assertNotNull(sessionCreation.getSession());
        assertNotNull(sessionCreation.getSessionSecret());

        final var session = sessionCreation.getSession();
        assertNotNull(session.getUser());
        assertNull(session.getProfile());
        assertNull(session.getApplication());

        response = client
                .target(format("%s/user/me", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", sessionCreation.getSessionSecret()))
                .get();

        final var user = response.readEntity(User.class);
        assertEquals(context.getUser().getId(), user.getId());

    }

    @Test(dataProvider = "getContexts", dependsOnMethods = "testUpdatePassword")
    public void testUpdatePasswordWithProfile(final ClientContext context) {

        final var request = new UserUpdatePasswordRequest();
        request.setNewPassword(SECOND_UPDATE);
        request.setOldPassword(FIRST_UPDATE);
        request.setProfileId(context.getDefaultProfile().getId());

        context.createSession();

        var response = client
                .target(format("%s/user/%s/password", apiRoot, context.getUser().getId()))
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        final var sessionCreation = response.readEntity(SessionCreation.class);
        assertNotNull(sessionCreation.getSession());
        assertNotNull(sessionCreation.getSessionSecret());

        final var session = sessionCreation.getSession();
        assertNotNull(session.getUser());
        assertNotNull(session.getProfile());
        assertNotNull(session.getApplication());

        response = client
                .target(format("%s/user/me", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", sessionCreation.getSessionSecret()))
                .get();

        final var user = response.readEntity(User.class);
        assertEquals(context.getUser().getId(), user.getId());

    }

    @Test(dataProvider = "getContexts", dependsOnMethods = "testUpdatePasswordWithProfile")
    public void testNewPasswordWorksUserName(final ClientContext context) {

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(context.getUser().getName());
        request.setProfileId(context.getDefaultProfile().getId());
        request.setPassword(SECOND_UPDATE);

        var response = client
                .target(format("%s/session", apiRoot))
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        final var sessionCreation = response.readEntity(SessionCreation.class);
        assertNotNull(sessionCreation.getSession());
        assertNotNull(sessionCreation.getSessionSecret());

        final var session = sessionCreation.getSession();
        assertNotNull(session.getUser());
        assertNotNull(session.getProfile());
        assertNotNull(session.getApplication());

        response = client
                .target(format("%s/user/me", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", sessionCreation.getSessionSecret()))
                .get();

        final var user = response.readEntity(User.class);
        assertEquals(context.getUser().getId(), user.getId());

    }

    @Test(dataProvider = "getContexts", dependsOnMethods = "testUpdatePasswordWithProfile")
    public void testNewPasswordWorksUserEmail(final ClientContext context) {

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(context.getUser().getEmail());
        request.setProfileId(context.getDefaultProfile().getId());
        request.setPassword(SECOND_UPDATE);

        var response = client
                .target(format("%s/session", apiRoot))
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        final var sessionCreation = response.readEntity(SessionCreation.class);
        assertNotNull(sessionCreation.getSession());
        assertNotNull(sessionCreation.getSessionSecret());

        final var session = sessionCreation.getSession();
        assertNotNull(session.getUser());
        assertNotNull(session.getProfile());
        assertNotNull(session.getApplication());

        response = client
                .target(format("%s/user/me", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", sessionCreation.getSessionSecret()))
                .get();

        final var user = response.readEntity(User.class);
        assertEquals(context.getUser().getId(), user.getId());

    }

    @DataProvider
    public Object[][] getCrossUserContexts() {
        return new Object[][] {
                {user0, user1},
                {user1, user0}
        };
    }

    @Test(dataProvider = "getCrossUserContexts", dependsOnMethods = "testNewPasswordWorks")
    public void testUpdateCrossUserFails(final ClientContext authorized, final ClientContext unauthorized) {

        final var request = new UserUpdatePasswordRequest();
        request.setNewPassword(ALWAYS_FAILS);
        request.setOldPassword(SECOND_UPDATE);
        request.setProfileId(authorized.getDefaultProfile().getId());

        authorized.createSession();
        unauthorized.createSession();

        var response = client
                .target(format("%s/user/%s/password", apiRoot, authorized.getUser().getId()))
                .request()
                .header("Authorization", format("Bearer %s", unauthorized.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(404, response.getStatus());

    }

}

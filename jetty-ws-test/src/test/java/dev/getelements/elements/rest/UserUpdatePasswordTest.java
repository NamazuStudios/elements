package dev.getelements.elements.rest;

import dev.getelements.elements.model.session.SessionCreation;
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
        user0.createUser("password-test").createSession();
        user1.createUser("password-test").createSession();
    }

    @DataProvider
    public Object[][] getContexts() {
        return new Object[][] { {user0}, {user1} };
    }

    @Test(dataProvider = "getContexts")
    public void testUpdatePassword(final ClientContext context) {

        final var request = new UserUpdatePasswordRequest();
        request.setNewPassword("updatedpassword");
        request.setOldPassword(ClientContext.DUMMY_PASSWORD);

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

}

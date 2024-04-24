package dev.getelements.elements.rest.test;

import dev.getelements.elements.dao.FirebaseApplicationConfigurationDao;
import dev.getelements.elements.model.application.ConfigurationCategory;
import dev.getelements.elements.model.application.FirebaseApplicationConfiguration;
import dev.getelements.elements.model.session.FirebaseSessionRequest;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rest.test.ClientContext;
import dev.getelements.elements.rest.test.EmbeddedElementsWebServices;
import dev.getelements.elements.rest.test.TestUtils;
import dev.getelements.elements.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

import static dev.getelements.elements.service.FirebaseServiceAccountCredentials.loadServiceAccountCredentials;
import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.Headers.SOCIALENGINE_SESSION_SECRET;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.security.AuthorizationHeader.AUTH_HEADER;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class FirebaseJWTIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseJWTIntegrationTest.class);

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(FirebaseJWTIntegrationTest.class),
            TestUtils.getInstance().getUnixFSTest(FirebaseJWTIntegrationTest.class)
        };
    }

    private final FirebaseTestClient ftc = new FirebaseTestClient();

    private FirebaseEmailPasswordSignUpResponse signupResult;

    private FirebaseUsernamePasswordSignInResponse signinResult;

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private EmbeddedElementsWebServices embeddedElementsWebServices;

    @Inject
    private Client client;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    @Inject
    private FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao;

    private SessionCreation sessionCreation;

    @Test
    public void signupUser() {

        final var signup = ftc.randomUserSignup();
        signupResult = ftc.signUp(signup);
        logger.info("Successfully created user.");

        final var signin = new FirebaseUsernamePasswordSignInRequest(signup);
        signinResult = ftc.signIn(signin);
        logger.info("Successfully logged-in user.");

    }

    @Test(dependsOnMethods = "signupUser")
    public void createFirebaseConfiguration() {
        final var application = clientContextProvider.get().getApplication();
        final var configuration = new FirebaseApplicationConfiguration();
        configuration.setParent(application);
        configuration.setProjectId("elements-integration-test");
        configuration.setUniqueIdentifier("elements-integration-test");
        configuration.setCategory(ConfigurationCategory.FIREBASE);
        configuration.setServiceAccountCredentials(loadServiceAccountCredentials());
        firebaseApplicationConfigurationDao.createOrUpdateInactiveApplicationConfiguration(application.getId(), configuration);
    }

    @Test(dependsOnMethods = "createFirebaseConfiguration")
    public void createSession() {

        final var request = new FirebaseSessionRequest();
        request.setFirebaseJWT(signupResult.getIdToken());

        sessionCreation = client
            .target(apiRoot + "/firebase_session")
            .request()
            .post(Entity.entity(request, APPLICATION_JSON_TYPE))
            .readEntity(SessionCreation.class);

        if (sessionCreation.getSessionSecret() == null) {
            logger.info("Test");
        }

        assertNotNull(sessionCreation.getSessionSecret());

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

    @Test(dependsOnMethods = "createSession", dataProvider = "getAuthHeader")
    public void ensureSessionIsValid(final String authHeader, final String authHeaderFormat) {

        final var authHeaderValue = format(authHeaderFormat, sessionCreation.getSessionSecret());

        final var user = client
            .target(apiRoot + "/user/me")
            .request()
            .header(authHeader, authHeaderValue)
            .get(User.class);

        assertEquals(user.getEmail(), signinResult.getEmail());
        assertEquals(user.getFirebaseId(), signinResult.getLocalId());

    }

    @Test(dependsOnMethods = "ensureSessionIsValid")
    public void destroyAccount() {
        final var request = new FirebaseDeleteAccountRequest();
        request.setIdToken(signinResult.getIdToken());
        ftc.deleteAccount(request);
        logger.info("Successfully deleted account.");
    }

}

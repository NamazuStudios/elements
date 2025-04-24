package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.profile.Profile;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.model.Headers.*;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ProfileOverrideIntegrationTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getTestFixture(ProfileOverrideIntegrationTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private EmbeddedElementsWebServices embeddedElementsWebServices;

    @Inject
    private Client client;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    private ClientContext clientContext;

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
            new Object[] { SESSION_SECRET },
            new Object[] { SOCIALENGINE_SESSION_SECRET }
        };
    }

    @BeforeClass
    private void setUp() {
        clientContext = clientContextProvider.get()
            .createUser("ProfileOverrideIntegrationTest")
            .createProfiles(10)
            .createSession();
    }

    @Test(dataProvider = "getAuthHeader")
    public void testOverrideProfileFailure(final String authHeader) throws Exception {
        try {
            client.target(apiRoot + "/profile/current")
                  .request()
                  .header(authHeader, clientContext.getSessionSecret())
                  .buildGet()
                  .submit(Profile.class)
              .get();
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof NotFoundException, "Expected " + NotFoundException.class.getName());
        }
    }

    @DataProvider
    public Object[][] provideProfiles() {

        final Stream<Object[]> current = clientContext.getProfiles()
            .stream()
            .map(p -> new Object[]{p, SESSION_SECRET});

        final Stream<Object[]> legacy = clientContext.getProfiles()
            .stream()
            .map(p -> new Object[]{p, SOCIALENGINE_SESSION_SECRET});

        return Stream.concat(current, legacy).toArray(Object[][]::new);

    }

    @Test(dataProvider = "provideProfiles", dependsOnMethods = "testOverrideProfileFailure")
    public void testOverrideProfileProfileIdHeader(final Profile profile,
                                                   final String authHeader) throws Exception {

        final Profile current = client
                .target(apiRoot + "/profile/current")
                .request()
                .header(authHeader, clientContext.getSessionSecret())
                .header(PROFILE_ID, profile.getId())
                .buildGet()
                .submit(Profile.class)
            .get();

        assertEquals(current.getId(), profile.getId());

    }

}

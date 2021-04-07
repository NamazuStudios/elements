package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.profile.Profile;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.Headers.*;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ProfileOverrideIntegrationTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(ProfileOverrideIntegrationTest.class),
            TestUtils.getInstance().getUnixFSTest(ProfileOverrideIntegrationTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private EmbeddedRestApi embeddedRestApi;

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

    @Test(dataProvider = "provideProfiles", dependsOnMethods = "testOverrideProfileFailure")
    public void testOverrideProfileSessionSecretHeader(final Profile profile,
                                                       final String authHeader) throws Exception {

        final String sessionSecretHeader = format("%s p%s", clientContext.getSessionSecret(), profile.getId());

        final Profile current = client
                .target(apiRoot + "/profile/current")
                .request()
                .header(authHeader, sessionSecretHeader)
                .buildGet()
                .submit(Profile.class)
            .get();

        assertEquals(current.getId(), profile.getId());

    }

}

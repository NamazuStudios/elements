package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.profile.Profile;
import org.testng.annotations.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;

import java.util.concurrent.ExecutionException;

import static com.namazustudios.socialengine.Headers.PROFILE_ID;
import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


@Guice(modules = {EmbeddedRestApiIntegrationTestModule.class})
public class ProfileOverrideIntegrationTest {

    @Inject
    private EmbeddedRestApi embeddedRestApi;

    @Inject
    private Client client;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    private ClientContext clientContext;

    @BeforeClass
    private void setUp() throws Exception {
        embeddedRestApi.start();
        clientContext = clientContextProvider.get()
            .createUser("ProfileOverrideIntegrationTest")
            .createProfiles(10)
            .createSession();
    }

    @AfterClass
    public void tearDown() throws Exception {
        embeddedRestApi.stop();
    }

    @Test
    public void testOverrideProfileFailure() throws Exception {
        try {
            client.target("http://localhost:8080/api/rest/profile/current")
                  .request()
                  .header(SESSION_SECRET, clientContext.getSessionSecret())
                  .buildGet()
                  .submit(Profile.class)
                  .get();
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof NotFoundException, "Expected " + NotFoundException.class.getName());
        }
    }

    @DataProvider
    public Object[][] provideProfiles() {
        return clientContext.getProfiles()
            .stream()
            .map(p -> new Object[]{p})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "provideProfiles", dependsOnMethods = "testOverrideProfileFailure")
    public void testOverrideProfileProfileIdHeader(final Profile profile) throws Exception {

        final Profile current = client
            .target("http://localhost:8080/api/rest/profile/current")
            .request()
            .header(SESSION_SECRET, clientContext.getSessionSecret())
            .header(PROFILE_ID, profile.getId())
            .buildGet()
            .submit(Profile.class)
            .get();

        assertEquals(current.getId(), profile.getId());

    }

    @Test(dataProvider = "provideProfiles", dependsOnMethods = "testOverrideProfileFailure")
    public void testOverrideProfileSessionSecretHeader(final Profile profile) throws Exception {

        final String sessionSecretHeader = format("%s p%s", clientContext.getSessionSecret(), profile.getId());

        final Profile current = client
                .target("http://localhost:8080/api/rest/profile/current")
                .request()
                .header(SESSION_SECRET, sessionSecretHeader)
                .buildGet()
                .submit(Profile.class)
                .get();

        assertEquals(current.getId(), profile.getId());

    }

}

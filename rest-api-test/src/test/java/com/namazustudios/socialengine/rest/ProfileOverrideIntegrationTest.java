package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.Profile;
import org.testng.annotations.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;

import static com.namazustudios.socialengine.Headers.PROFILE_ID;
import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static org.testng.Assert.assertEquals;


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

    @DataProvider
    public Object[][] provideProfiles() {
        return clientContext.getProfiles()
            .stream()
            .map(p -> new Object[]{p})
            .toArray(Object[][]::new);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testOverrideProfile() throws Exception {
        client
            .target("http://localhost:8080/api/rest/profile/current")
            .request()
            .header(SESSION_SECRET, clientContext.getSessionSecret())
            .buildGet()
            .submit(Profile.class)
            .get();
    }

    @Test(dataProvider = "provideProfiles")
    public void testOverrideProfile(final Profile profile) throws Exception {

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

    private static class ProfilePagination extends Pagination<Profile> {}

}

package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import org.testng.annotations.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.client.Client;

import java.util.Set;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.testng.Assert.*;


@Guice(modules = {EmbeddedRestApiIntegrationTestModule.class})
public class GetProfilesIntegrationTest {

    @Inject
    private EmbeddedRestApi embeddedRestApi;

    @Inject
    private Client client;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    private ClientContext client0;

    private ClientContext client1;

    @BeforeClass
    private void setUp() throws Exception {
        embeddedRestApi.start();
        client0 = clientContextProvider.get()
            .createUser("GetProfileIntegrationTest0")
            .createProfiles(5)
            .createSession();
        client1 = clientContextProvider.get()
            .createUser("GetProfileIntegrationTest1")
            .createProfiles(5)
            .createSession();
    }

    @AfterClass
    public void tearDown() throws Exception {
        embeddedRestApi.stop();
    }

    @DataProvider
    public Object[][] provideClientContexts() {
        return new Object[][] {
            new Object[]{client0},
            new Object[]{client1},
        };
    }

    @DataProvider
    public Object[][] provideClientContextsAndNonmatchingUsers() {
        return new Object[][] {
                new Object[]{client0, client1.getUser()},
                new Object[]{client1, client0.getUser()},
        };
    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetAllProfiles(final ClientContext clientContext) throws Exception {

        final Pagination<Profile> profiles = client
              .target("http://localhost:8080/api/rest/profile")
              .queryParam("count", 20)
              .request()
              .header(SESSION_SECRET, clientContext.getSessionSecret())
              .buildGet()
              .submit(ProfilePagination.class)
              .get();

        assertEquals(profiles.getTotal(), 10);
        assertEquals(profiles.getObjects().size(), 10);
        for (final Profile profile : profiles.getObjects()) assertNull(profile.getUser());

    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByUserId(final ClientContext clientContext) throws Exception {

        Pagination<Profile> profiles;
        profiles = client
                .target("http://localhost:8080/api/rest/profile")
                .queryParam("count", 20)
                .queryParam("user", clientContext.getUser().getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        final Set<String> profileIds = clientContext.getProfiles().stream().map(p -> p.getId()).collect(toSet());

        assertEquals(profiles.getTotal(), 5);
        assertEquals(profiles.getObjects().size(), 5);
        profiles.getObjects().forEach(p -> assertTrue(profileIds.contains(p.getId())));

    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByUserIdMe(final ClientContext clientContext) throws Exception {

        Pagination<Profile> profiles;
        profiles = client
                .target("http://localhost:8080/api/rest/profile")
                .queryParam("count", 20)
                .queryParam("user", "me")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        final Set<String> profileIds = clientContext.getProfiles().stream().map(p -> p.getId()).collect(toSet());

        assertEquals(profiles.getTotal(), 5);
        assertEquals(profiles.getObjects().size(), 5);
        profiles.getObjects().forEach(p -> assertTrue(profileIds.contains(p.getId())));

    }

    @Test(dataProvider = "provideClientContextsAndNonmatchingUsers")
    public void testGetProfilesFilteredByValidNonMatchingUserId(final ClientContext clientContext,
                                                                final User user) throws Exception {

        Pagination<Profile> profiles;
        profiles = client
                .target("http://localhost:8080/api/rest/profile")
                .queryParam("count", 20)
                .queryParam("user", user.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        final Set<String> profileIds = clientContext.getProfiles().stream().map(p -> p.getId()).collect(toSet());

        assertEquals(profiles.getTotal(), 0);
        assertEquals(profiles.getObjects().size(), 0);
        profiles.getObjects().forEach(p -> assertTrue(profileIds.contains(p.getId())));

    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByInvalidNonMatchingUserId(final ClientContext clientContext) throws Exception {

        Pagination<Profile> profiles;
        profiles = client
                .target("http://localhost:8080/api/rest/profile")
                .queryParam("count", 20)
                .queryParam("user", "bogo")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        assertEquals(profiles.getTotal(), 0);
        assertEquals(profiles.getObjects().size(), 0);

    }

}

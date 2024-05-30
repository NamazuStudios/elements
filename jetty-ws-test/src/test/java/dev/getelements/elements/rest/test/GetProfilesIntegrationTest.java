package dev.getelements.elements.rest.test;

import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rest.test.ClientContext;
import dev.getelements.elements.rest.test.ProfilePagination;
import dev.getelements.elements.rest.test.TestUtils;
import org.testng.annotations.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.client.Client;
import java.util.Set;

import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.Headers.SOCIALENGINE_SESSION_SECRET;
import static dev.getelements.elements.rest.test.TestUtils.*;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.*;

public class GetProfilesIntegrationTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(GetProfilesIntegrationTest.class),
                TestUtils.getInstance().getUnixFSTest(GetProfilesIntegrationTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ApplicationDao applicationDao;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    private Application other;

    private ClientContext client0;

    private ClientContext client1;

    @BeforeClass
    private void setUp() {

        other = new Application();
        other.setName("OTHER");
        other.setDescription("Other Application (not under test)");
        other = applicationDao.createOrUpdateInactiveApplication(other);

        client0 = clientContextProvider.get()
            .createUser("GetProfileIntegrationTest0")
            .createProfiles(5)
            .createSession();

        client1 = clientContextProvider.get()
            .createUser("GetProfileIntegrationTest1")
            .createProfiles(5)
            .createSession();

    }

    @DataProvider
    public Object[][] provideClientContexts() {
        return new Object[][] {
            new Object[]{client0, SESSION_SECRET},
            new Object[]{client1, SOCIALENGINE_SESSION_SECRET},
        };
    }

    @DataProvider
    public Object[][] provideClientContextsAndNonmatchingUsers() {
        return new Object[][] {
                new Object[]{client0, client1.getUser(), SESSION_SECRET},
                new Object[]{client1, client0.getUser(), SOCIALENGINE_SESSION_SECRET},
        };
    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetAllProfiles(final ClientContext clientContext, final String authHeader) throws Exception {

        final Pagination<Profile> profiles = client
              .target(apiRoot + "/profile")
              .queryParam("count", 20)
              .request()
              .header(authHeader, clientContext.getSessionSecret())
              .buildGet()
              .submit(ProfilePagination.class)
              .get();

        assertTrue(profiles.getTotal() >= 10);
        assertTrue(profiles.getObjects().size() >= 10);
        for (final Profile profile : profiles.getObjects()) assertNull(profile.getUser());

    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByUserId(final ClientContext clientContext,
                                                final String authHeader) throws Exception {

        Pagination<Profile> profiles;
        profiles = client
                .target(apiRoot + "/profile")
                .queryParam("count", 20)
                .queryParam("user", clientContext.getUser().getId())
                .request()
                .header(authHeader, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
            .get();

        final Set<String> profileIds = clientContext.getProfiles().stream().map(p -> p.getId()).collect(toSet());

        assertEquals(profiles.getTotal(), 5);
        assertEquals(profiles.getObjects().size(), 5);
        profiles.getObjects().forEach(p -> assertTrue(profileIds.contains(p.getId())));

    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByUserIdAndApplicationId(final ClientContext clientContext,
                                                                final String authHeader) throws Exception {

        Pagination<Profile> profiles;

        profiles = client
                .target(apiRoot + "/profile")
                .queryParam("count", 20)
                .queryParam("user", clientContext.getUser().getId())
                .queryParam("application", clientContext.getApplication().getId())
                .request()
                .header(authHeader, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        final Set<String> profileIds = clientContext.getProfiles().stream().map(p -> p.getId()).collect(toSet());

        assertEquals(profiles.getTotal(), 5);
        assertEquals(profiles.getObjects().size(), 5);
        profiles.getObjects().forEach(p -> assertTrue(profileIds.contains(p.getId())));

    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByUserIdAndApplicationName(final ClientContext clientContext,
                                                                  final String authHeader) throws Exception {

        Pagination<Profile> profiles;

        profiles = client
                .target(apiRoot + "/profile")
                .queryParam("count", 20)
                .queryParam("user", clientContext.getUser().getId())
                .queryParam("application", clientContext.getApplication().getName())
                .request()
                .header(authHeader, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        final Set<String> profileIds = clientContext.getProfiles().stream().map(p -> p.getId()).collect(toSet());

        assertEquals(profiles.getTotal(), 5);
        assertEquals(profiles.getObjects().size(), 5);
        profiles.getObjects().forEach(p -> assertTrue(profileIds.contains(p.getId())));

    }


    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByUserIdMe(final ClientContext clientContext,
                                                  final String authHeader) throws Exception {

        Pagination<Profile> profiles;
        profiles = client
                .target(apiRoot + "/profile")
                .queryParam("count", 20)
                .queryParam("user", "me")
                .request()
                .header(authHeader, clientContext.getSessionSecret())
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
                                                                final User user,
                                                                final String authHeader) throws Exception {

        Pagination<Profile> profiles;
        profiles = client
                .target(apiRoot + "/profile")
                .queryParam("count", 20)
                .queryParam("user", user.getId())
                .request()
                .header(authHeader, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        final Set<String> profileIds = clientContext.getProfiles().stream().map(p -> p.getId()).collect(toSet());

        assertEquals(profiles.getTotal(), 0);
        assertEquals(profiles.getObjects().size(), 0);
        profiles.getObjects().forEach(p -> assertTrue(profileIds.contains(p.getId())));

    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByValidNonmatchingApplicationId(final ClientContext clientContext,
                                                                       final String authHeader) throws Exception {

        Pagination<Profile> profiles;

        profiles = client
                .target(apiRoot + "/profile")
                .queryParam("application", other.getId())
                .request()
                .header(authHeader, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        assertEquals(profiles.getTotal(), 0);
        assertEquals(profiles.getObjects().size(), 0);

        profiles = client
                .target(apiRoot + "/profile")
                .queryParam("application", other.getName())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        assertEquals(profiles.getTotal(), 0);
        assertEquals(profiles.getObjects().size(), 0);

    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByValidNonmatchingApplicationName(final ClientContext clientContext,
                                                                         final String authHeader) throws Exception {

        Pagination<Profile> profiles;

        profiles = client
                .target(apiRoot + "/profile")
                .queryParam("application", other.getName())
                .request()
                .header(authHeader, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        assertEquals(profiles.getTotal(), 0);
        assertEquals(profiles.getObjects().size(), 0);

    }

    @Test(dataProvider = "provideClientContexts")
    public void testGetProfilesFilteredByInvalidNonmatchingApplicationId(final ClientContext clientContext,
                                                                         final String authHeader) throws Exception {

        Pagination<Profile> profiles;
        profiles = client
                .target(apiRoot + "/profile")
                .queryParam("application", "bogo")
                .request()
                .header(authHeader, clientContext.getSessionSecret())
                .buildGet()
                .submit(ProfilePagination.class)
                .get();

        assertEquals(profiles.getTotal(), 0);
        assertEquals(profiles.getObjects().size(), 0);

    }

}

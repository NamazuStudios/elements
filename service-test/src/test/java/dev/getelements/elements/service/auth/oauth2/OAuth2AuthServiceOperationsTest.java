package dev.getelements.elements.service.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OAuth2AuthSchemeDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthLoginEvent;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.auth.OAuth2RequestKeyValue;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.SessionTokenIssuer;
import dev.getelements.elements.sdk.service.name.NameService;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static dev.getelements.elements.sdk.service.auth.OAuth2AuthService.OAUTH2_AUTH_LOGIN_EVENT;
import static java.util.Map.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Covers the {@code OAUTH2_AUTH_LOGIN_EVENT} contract (#93) across the {@code OAuth2SessionRequest}
 * permutations: the three profile-resolution paths (profileId, profileSelector, applicationNameOrId
 * with an existing/auto-created/no profile), no profile identifiers at all, the credential material
 * carried on the event (request parameters merged with the deprecated headers), the mapper-resolved
 * user (login vs. account-link), and the failure paths, which must not publish anything.
 */
public class OAuth2AuthServiceOperationsTest {

    private OAuth2AuthSchemeDao oAuth2AuthSchemeDao;

    private OAuth2AuthServiceRequestInvoker requestInvoker;

    private ProfileDao profileDao;

    private ApplicationDao applicationDao;

    private SessionTokenIssuer sessionTokenIssuer;

    private MutableElementRegistry elementRegistry;

    private List<Event> publishedEvents;

    private OAuth2AuthServiceOperations operations;

    @BeforeMethod
    public void setup() {

        oAuth2AuthSchemeDao = mock(OAuth2AuthSchemeDao.class);
        requestInvoker = mock(OAuth2AuthServiceRequestInvoker.class);
        profileDao = mock(ProfileDao.class);
        applicationDao = mock(ApplicationDao.class);
        sessionTokenIssuer = mock(SessionTokenIssuer.class);
        var nameService = mock(NameService.class);
        mock(Client.class);

        elementRegistry = MutableElementRegistry.newDefaultInstance();
        publishedEvents = new CopyOnWriteArrayList<>();
        elementRegistry.onEvent(ev -> {
            if (OAUTH2_AUTH_LOGIN_EVENT.equals(ev.getEventName())) {
                publishedEvents.add(ev);
            }
        });

        operations = new OAuth2AuthServiceOperations();
        operations.setoAuth2AuthSchemeDao(oAuth2AuthSchemeDao);
        operations.setRequestInvoker(requestInvoker);
        operations.setProfileDao(profileDao);
        operations.setApplicationDao(applicationDao);
        operations.setSessionTokenIssuer(sessionTokenIssuer);
        operations.setNameService(nameService);
        operations.setElementRegistry(elementRegistry);
        operations.setSessionTimeoutSeconds(60);

    }

    @AfterMethod
    public void closeRegistry() {
        elementRegistry.close();
    }

    // -- Good input: each profile-resolution path publishes exactly one event -----------------------

    @Test
    public void profileIdPathPublishesEventWithProfile() {
        stubResolution(200, json("active", true), "external-1");

        final var profile = profile("profile-1");
        when(profileDao.getActiveProfile("profile-1")).thenReturn(profile);

        final var request = baseRequest();
        request.setProfileId("profile-1");

        authenticate(request, "user-1");

        assertEquals(publishedEvents.size(), 1, "exactly one login event must be published");
        final var event = publishedEvents.get(0).getEventArgument(0, OAuth2AuthLoginEvent.class);
        assertEquals(event.user().getId(), "user-1");
        assertEquals(event.profile(), profile);
        assertEquals(event.scheme().getName(), "scheme-1");
        assertEquals(event.requestParameters(), Map.of("access_token", "t0k3n"));
    }

    @Test
    public void profileSelectorPathPublishesEventWithMatchedProfile() {
        stubResolution(200, json("active", true), "external-1");

        final var profile = profile("profile-1");
        when(profileDao.getActiveProfiles(0, 1, "selector")).thenReturn(pagination(profile));

        final var request = baseRequest();
        request.setProfileSelector("selector");

        authenticate(request, "user-1");

        assertEquals(publishedEvents.size(), 1);
        assertEquals(publishedEvents.get(0).getEventArgument(0, OAuth2AuthLoginEvent.class).profile(), profile);
    }

    @Test
    public void profileSelectorWithoutMatchPublishesEventWithoutProfile() {
        stubResolution(200, json("active", true), "external-1");

        when(profileDao.getActiveProfiles(0, 1, "selector")).thenReturn(pagination());

        final var request = baseRequest();
        request.setProfileSelector("selector");

        authenticate(request, "user-1");

        assertEquals(publishedEvents.size(), 1);
        assertNull(publishedEvents.get(0).getEventArgument(0, OAuth2AuthLoginEvent.class).profile(),
                "a login resolving no profile must publish a null-profile payload");
    }

    @Test
    public void applicationPathWithExistingPrimaryProfilePublishesEventWithProfile() {
        stubResolution(200, json("active", true), "external-1");

        final var application = application("application-1");
        when(applicationDao.findApplication("application-1")).thenReturn(java.util.Optional.of(application));

        final var profile = profile("profile-1");
        when(profileDao.findPrimaryProfile("user-1", "application-1")).thenReturn(java.util.Optional.of(profile));

        final var request = baseRequest();
        request.setApplicationNameOrId("application-1");

        authenticate(request, "user-1");

        assertEquals(publishedEvents.size(), 1);
        assertEquals(publishedEvents.get(0).getEventArgument(0, OAuth2AuthLoginEvent.class).profile(), profile);
    }

    @Test
    public void applicationPathWithConfiguredAutoCreatePublishesEventWithCreatedProfile() {
        stubResolution(200, json("active", true), "external-1");

        final var application = application("application-1");
        application.setAutoCreateProfile(true);
        application.setMaxProfiles(1);
        when(applicationDao.findApplication("application-1")).thenReturn(java.util.Optional.of(application));
        when(profileDao.findPrimaryProfile("user-1", "application-1")).thenReturn(java.util.Optional.empty());

        final var createdProfile = profile("profile-1");
        when(profileDao.createSlottedProfile(any(), any())).thenReturn(createdProfile);

        final var request = baseRequest();
        request.setApplicationNameOrId("application-1");

        authenticate(request, "user-1");

        assertEquals(publishedEvents.size(), 1);
        assertEquals(publishedEvents.get(0).getEventArgument(0, OAuth2AuthLoginEvent.class).profile(), createdProfile);
    }

    @Test
    public void applicationPathWithoutAutoCreatePublishesEventWithoutProfile() {
        stubResolution(200, json("active", true), "external-1");

        final var application = application("application-1");
        application.setAutoCreateProfile(false);
        when(applicationDao.findApplication("application-1")).thenReturn(java.util.Optional.of(application));
        when(profileDao.findPrimaryProfile("user-1", "application-1")).thenReturn(java.util.Optional.empty());

        final var request = baseRequest();
        request.setApplicationNameOrId("application-1");

        authenticate(request, "user-1");

        assertEquals(publishedEvents.size(), 1);
        assertNull(publishedEvents.get(0).getEventArgument(0, OAuth2AuthLoginEvent.class).profile(),
                "gated auto-create must not create a profile when the application does not allow it");
    }

    @Test
    public void requestWithoutProfileIdentifiersPublishesEventWithoutProfile() {
        stubResolution(200, json("active", true), "external-1");

        authenticate(baseRequest(), "user-1");

        assertEquals(publishedEvents.size(), 1);
        assertNull(publishedEvents.get(0).getEventArgument(0, OAuth2AuthLoginEvent.class).profile());
    }

    // -- Payload fidelity ---------------------------------------------------------------------------

    @Test
    public void deprecatedRequestHeadersAreMergedIntoTheEventCredentials() {
        stubResolution(200, json("active", true), "external-1");

        final var request = baseRequest();
        request.setRequestHeaders(Map.of("authorization", "Bearer hdr-token"));

        authenticate(request, "user-1");

        assertEquals(publishedEvents.size(), 1);
        final var event = publishedEvents.get(0).getEventArgument(0, OAuth2AuthLoginEvent.class);
        assertEquals(event.requestParameters(),
                Map.of("access_token", "t0k3n", "authorization", "Bearer hdr-token"));
    }

    @Test
    public void eventCarriesTheMapperResolvedUser() {
        // The mapper identity is the only distinction between the anon (login) and user (account-link)
        // service implementations: both funnel through the same operations method.
        stubResolution(200, json("active", true), "external-1");

        final var request = baseRequest();
        request.setProfileId("profile-1");
        when(profileDao.getActiveProfile("profile-1")).thenReturn(profile("profile-1"));

        when(sessionTokenIssuer.issue(any())).then(a -> {
            final var creation = new SessionCreation();
            creation.setSession(a.getArgument(0, dev.getelements.elements.sdk.model.session.Session.class));
            creation.setSessionSecret("secret");
            return creation;
        });

        operations.createOrUpdateUserWithToken(request, (schemeName, externalId) -> {
            final var linkedUser = new User();
            linkedUser.setId("linked-user-1");
            return linkedUser;
        });

        assertEquals(publishedEvents.size(), 1);
        assertEquals(publishedEvents.get(0).getEventArgument(0, OAuth2AuthLoginEvent.class).user().getId(),
                "linked-user-1",
                "the event must carry the mapper-resolved (linked) user");
    }

    // -- Bad input: failures must not publish --------------------------------------------------------

    @Test
    public void failedValidationDoesNotPublishEvent() {
        stubResolution(401, json("error", "invalid_token"), "external-1");

        assertNoEventOnFailure(baseRequest());
    }

    @Test
    public void duplicateClientKeyBetweenHeadersAndParametersDoesNotPublishEvent() {
        final var request = baseRequest();
        request.setRequestHeaders(Map.of("access_token", "different"));
        request.setRequestParameters(Map.of("access_token", "t0k3n"));

        assertNoEventOnFailure(request);
    }

    private void assertNoEventOnFailure(final OAuth2SessionRequest request) {
        try {
            operations.createOrUpdateUserWithToken(request, (schemeName, externalId) -> {
                final var user = new User();
                user.setId("user-1");
                return user;
            });
        } catch (Exception ignored) {
            // The failure itself is asserted elsewhere; here we only care that nothing was published.
        }

        assertEquals(publishedEvents.size(), 0, "a failed authentication must not publish a login event");
        verify(sessionTokenIssuer, never()).issue(any());
    }

    // -- Fixtures ------------------------------------------------------------------------------------

    private OAuth2SessionRequest baseRequest() {

        final var request = new OAuth2SessionRequest();
        request.setSchemeId("scheme-1");
        request.setRequestParameters(Map.of("access_token", "t0k3n"));
        return request;

    }

    private void stubResolution(final int status, final String responseJson, final String externalUserId) {

        final var scheme = new OAuth2AuthScheme();
        scheme.setName("scheme-1");
        scheme.setId("scheme-1");
        scheme.setValidStatusCodes(List.of(200));
        scheme.setResponseIdMapping("sub");

        when(oAuth2AuthSchemeDao.getAuthScheme("scheme-1")).thenReturn(scheme);
        when(requestInvoker.execute(any(), any())).thenReturn(
                new dev.getelements.elements.service.auth.oauth2.ParsedResponse(
                        status,
                        "{\"sub\":\"" + externalUserId + "\",\"active\":\"true\"}",
                        jsonNode("{\"sub\":\"" + externalUserId + "\",\"active\":\"true\"}")));

    }

    private void authenticate(final OAuth2SessionRequest request, final String userId) {
        when(sessionTokenIssuer.issue(any())).then(a -> {
            final var creation = new SessionCreation();
            creation.setSession(a.getArgument(0, dev.getelements.elements.sdk.model.session.Session.class));
            creation.setSessionSecret("secret");
            return creation;
        });

        operations.createOrUpdateUserWithToken(request, (schemeName, externalId) -> {
            final var user = new User();
            user.setId(userId);
            return user;
        });
    }

    private static String json(final String key, final Object value) {
        return "{\"" + key + "\":\"" + value + "\"}";
    }

    private static com.fasterxml.jackson.databind.JsonNode jsonNode(final String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static dev.getelements.elements.sdk.model.Pagination<Profile> pagination(final Profile... profiles) {
        final var pagination = new dev.getelements.elements.sdk.model.Pagination<Profile>();
        pagination.setObjects(List.of(profiles));
        return pagination;
    }

    private static Profile profile(final String id) {
        final var profile = new Profile();
        profile.setId(id);
        return profile;
    }

    private static Application application(final String id) {
        final var application = new Application();
        application.setId(id);
        return application;
    }

}

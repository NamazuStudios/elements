package dev.getelements.elements.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.auth.BodyType;
import dev.getelements.elements.sdk.model.auth.HttpMethod;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.auth.OAuth2RequestKeyValue;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.service.auth.oauth2.AnonOAuth2AuthService;
import dev.getelements.elements.service.auth.oauth2.OAuth2AuthServiceRequestInvoker;
import dev.getelements.elements.service.auth.oauth2.ParsedResponse;
import dev.getelements.elements.service.auth.oauth2.ResolvedRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class AnonOAuth2AuthServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SCHEME_NAME = "AnonTestScheme";
    private static final String EXT_USER_ID = "ext_alice";
    private static final String EXT_USER_ID_2 = "ext_bob";

    @Inject private AnonOAuth2AuthService service;
    @Inject private UserUidDao userUidDao;
    @Inject private UserDao userDao;
    @Inject private OAuth2AuthSchemeDao schemeDao;
    @Inject private OAuth2AuthServiceRequestInvoker invoker;
    @Inject private SessionDao sessionDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
        when(sessionDao.create(any(Session.class))).thenReturn(new SessionCreation());
        final var responseNode = MAPPER.createObjectNode();
        responseNode.put("is_valid", true);
        when(invoker.execute(any(), any(ResolvedRequest.class)))
                .thenReturn(new ParsedResponse(200, responseNode.toString(), responseNode));
    }

    /**
     * Anonymous user, uid/scheme not in db → creates a new user via createUserStrict and a new UserUid.
     */
    @Test
    public void testAnonymous_newUid_createsUserAndUid() {
        final var scheme = metaQuestScheme(SCHEME_NAME);
        when(schemeDao.getAuthScheme("scheme-anon-1")).thenReturn(scheme);
        when(userUidDao.findUserUid(EXT_USER_ID, SCHEME_NAME)).thenReturn(Optional.empty());

        final var newUser = userWithId("new-user-id");
        when(userDao.createUserStrict(any(User.class))).thenReturn(newUser);

        service.createSession(metaQuestRequest("scheme-anon-1", EXT_USER_ID));

        verify(userDao).createUserStrict(any(User.class));
        verify(userDao, never()).createUser(any(User.class));

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao).createUserUid(uidCaptor.capture());
        assertEquals(uidCaptor.getValue().getId(), EXT_USER_ID);
        assertEquals(uidCaptor.getValue().getScheme(), SCHEME_NAME);
        assertEquals(uidCaptor.getValue().getUserId(), "new-user-id");
    }

    /**
     * Anonymous user, uid/scheme already in db → returns the existing user, no new user or uid created.
     */
    @Test
    public void testAnonymous_existingUid_returnsExistingUser() {
        final var scheme = metaQuestScheme(SCHEME_NAME);
        when(schemeDao.getAuthScheme("scheme-anon-2")).thenReturn(scheme);

        final var existingUser = userWithId("existing-user-id");
        when(userUidDao.findUserUid(EXT_USER_ID, SCHEME_NAME))
                .thenReturn(Optional.of(uidFor(EXT_USER_ID, SCHEME_NAME, "existing-user-id")));
        when(userDao.getUser("existing-user-id")).thenReturn(existingUser);

        service.createSession(metaQuestRequest("scheme-anon-2", EXT_USER_ID));

        verify(userDao, never()).createUserStrict(any(User.class));
        verify(userDao, never()).createUser(any(User.class));
        verify(userUidDao, never()).createUserUid(any(UserUid.class));

        final var sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getUser().getId(), "existing-user-id");
    }

    /**
     * Two anonymous logins with different external UIDs must each create their own distinct user.
     * Regression test: createUser (upsert-by-name/email) would collapse both onto the same document
     * when name and email are blank; createUserStrict (plain insert) must be used instead.
     */
    @Test
    public void testAnonymous_twoDistinctUids_createDistinctUsers() {
        final var scheme = metaQuestScheme(SCHEME_NAME);
        when(schemeDao.getAuthScheme("scheme-anon-3")).thenReturn(scheme);
        when(userUidDao.findUserUid(EXT_USER_ID, SCHEME_NAME)).thenReturn(Optional.empty());
        when(userUidDao.findUserUid(EXT_USER_ID_2, SCHEME_NAME)).thenReturn(Optional.empty());

        final var userA = userWithId("user-a-id");
        final var userB = userWithId("user-b-id");
        when(userDao.createUserStrict(any(User.class))).thenReturn(userA).thenReturn(userB);

        service.createSession(metaQuestRequest("scheme-anon-3", EXT_USER_ID));
        service.createSession(metaQuestRequest("scheme-anon-3", EXT_USER_ID_2));

        // Each login must insert a fresh user — createUserStrict called twice, createUser never
        verify(userDao, times(2)).createUserStrict(any(User.class));
        verify(userDao, never()).createUser(any(User.class));

        // Each login must create its own UserUid pointing to a different user
        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao, times(2)).createUserUid(uidCaptor.capture());
        final var uids = uidCaptor.getAllValues();
        assertNotEquals(uids.get(0).getUserId(), uids.get(1).getUserId(),
                "Different external UIDs must produce different Elements users");
    }

    // ---------- helpers ----------

    /**
     * Scheme matching the MetaQuest structure used in QA:
     * POST + JSON body type, user_id and nonce as fromClient query params, static access_token,
     * responseValidMapping on "is_valid".
     */
    private static OAuth2AuthScheme metaQuestScheme(String name) {
        final var s = new OAuth2AuthScheme();
        s.setId("ignored");
        s.setName(name);
        s.setValidationUrl("https://graph.oculus.com/user_nonce_validate");
        s.setMethod(HttpMethod.POST);
        s.setBodyType(BodyType.JSON);
        s.setHeaders(List.of());
        s.setParams(List.of(
            new OAuth2RequestKeyValue("user_id", null, true, true),
            new OAuth2RequestKeyValue("nonce", null, true, false),
            new OAuth2RequestKeyValue("access_token", "OC|test|secret", false, false)
        ));
        s.setBody(List.of());
        s.setResponseValidMapping("is_valid");
        s.setValidStatusCodes(List.of(200));
        return s;
    }

    private static OAuth2SessionRequest metaQuestRequest(String schemeId, String userId) {
        final var r = new OAuth2SessionRequest();
        r.setSchemeId(schemeId);
        r.setRequestParameters(Map.of("user_id", userId, "nonce", "test-nonce-value"));
        r.setRequestHeaders(Map.of());
        return r;
    }

    private static User userWithId(String id) {
        final var u = new User();
        u.setId(id);
        return u;
    }

    private static UserUid uidFor(String id, String scheme, String userId) {
        final var u = new UserUid();
        u.setId(id);
        u.setScheme(scheme);
        u.setUserId(userId);
        return u;
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserUidDao.class).toInstance(mock(UserUidDao.class));
            bind(UserDao.class).toInstance(mock(UserDao.class));
            bind(OAuth2AuthSchemeDao.class).toInstance(mock(OAuth2AuthSchemeDao.class));
            bind(SessionDao.class).toInstance(mock(SessionDao.class));
            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(NameService.class).toInstance(mock(NameService.class));
            bind(ApplicationDao.class).toInstance(mock(ApplicationDao.class));
            bind(Client.class).toInstance(mock(Client.class));
            bind(OAuth2AuthServiceRequestInvoker.class).toInstance(mock(OAuth2AuthServiceRequestInvoker.class));
            bindConstant().annotatedWith(Names.named(SESSION_TIMEOUT_SECONDS)).to(3600L);
            bind(String.class).annotatedWith(named(API_OUTSIDE_URL)).toInstance("http://localhost:8080/api/rest");
        }
    }
}

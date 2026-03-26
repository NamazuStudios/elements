package dev.getelements.elements.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.auth.BodyType;
import dev.getelements.elements.sdk.model.auth.HttpMethod;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.auth.OAuth2RequestKeyValue;
import dev.getelements.elements.sdk.model.exception.auth.AuthValidationException;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.service.auth.oauth2.OAuth2AuthServiceRequestInvoker;
import dev.getelements.elements.service.auth.oauth2.ParsedResponse;
import dev.getelements.elements.service.auth.oauth2.ResolvedRequest;
import dev.getelements.elements.service.auth.oauth2.UserOAuth2AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class UserOAuth2AuthServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SCHEME_NAME = "UserTestScheme";
    private static final String EXT_USER_ID = "ext_bob";
    private static final String CURRENT_USER_ID = "current-user-id";

    @Inject private UserOAuth2AuthService service;
    @Inject private UserUidDao userUidDao;
    @Inject private UserDao userDao;
    @Inject private OAuth2AuthSchemeDao schemeDao;
    @Inject private OAuth2AuthServiceRequestInvoker invoker;
    @Inject private SessionDao sessionDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
        setupMocks();
    }

    private void setupMocks() {
        when(sessionDao.create(any(Session.class))).thenReturn(new SessionCreation());
        final var responseNode = MAPPER.createObjectNode();
        responseNode.put("is_valid", true);
        when(invoker.execute(any(), any(ResolvedRequest.class)))
                .thenReturn(new ParsedResponse(200, responseNode.toString(), responseNode));
    }

    /**
     * Authenticated user, uid/scheme already linked to the same user → returns that user, no changes made.
     */
    @Test
    public void testAuthenticated_uidFoundSameUser_returnsUser() {
        final var scheme = metaQuestScheme(SCHEME_NAME);
        when(schemeDao.getAuthScheme("scheme-user-1")).thenReturn(scheme);

        when(userUidDao.findUserUid(EXT_USER_ID, SCHEME_NAME))
                .thenReturn(Optional.of(uidFor(EXT_USER_ID, SCHEME_NAME, CURRENT_USER_ID)));
        when(userDao.getUser(CURRENT_USER_ID)).thenReturn(userWithId(CURRENT_USER_ID));

        service.createSession(metaQuestRequest("scheme-user-1", EXT_USER_ID));

        verify(userUidDao, never()).createUserUid(any());
        verify(userDao, never()).createUser(any());

        final var sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getUser().getId(), CURRENT_USER_ID);
    }

    /**
     * Authenticated user, uid/scheme already linked to a DIFFERENT user → throws AuthValidationException.
     */
    @Test
    public void testAuthenticated_uidFoundDifferentUser_throwsException() {
        final var scheme = metaQuestScheme(SCHEME_NAME);
        when(schemeDao.getAuthScheme("scheme-user-2")).thenReturn(scheme);

        when(userUidDao.findUserUid(EXT_USER_ID, SCHEME_NAME))
                .thenReturn(Optional.of(uidFor(EXT_USER_ID, SCHEME_NAME, "other-user-id")));
        when(userDao.getUser("other-user-id")).thenReturn(userWithId("other-user-id"));

        assertThrows(AuthValidationException.class,
                () -> service.createSession(metaQuestRequest("scheme-user-2", EXT_USER_ID)));

        verify(sessionDao, never()).create(any());
        verify(userUidDao, never()).createUserUid(any());
    }

    /**
     * Authenticated user, uid/scheme not yet in db → links the external uid to the current user,
     * no new user created.
     */
    @Test
    public void testAuthenticated_uidNotFound_linksToCurrentUser() {
        final var scheme = metaQuestScheme(SCHEME_NAME);
        when(schemeDao.getAuthScheme("scheme-user-3")).thenReturn(scheme);
        when(userUidDao.findUserUid(EXT_USER_ID, SCHEME_NAME)).thenReturn(Optional.empty());

        service.createSession(metaQuestRequest("scheme-user-3", EXT_USER_ID));

        verify(userDao, never()).createUser(any());

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao).createUserUid(uidCaptor.capture());
        assertEquals(uidCaptor.getValue().getId(), EXT_USER_ID);
        assertEquals(uidCaptor.getValue().getScheme(), SCHEME_NAME);
        assertEquals(uidCaptor.getValue().getUserId(), CURRENT_USER_ID);

        final var sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getUser().getId(), CURRENT_USER_ID);
    }

    /**
     * Authenticated user, uid/scheme not in db but user already has a different uid for this scheme
     * → throws AuthValidationException; does not create a second uid for the same provider.
     */
    @Test
    public void testAuthenticated_alreadyLinkedSameScheme_throwsException() {
        // Re-inject with a user that already has SCHEME_NAME in linkedAccounts
        createInjector(new TestModule(Set.of(SCHEME_NAME))).injectMembers(this);
        setupMocks();

        final var scheme = metaQuestScheme(SCHEME_NAME);
        when(schemeDao.getAuthScheme("scheme-user-4")).thenReturn(scheme);
        when(userUidDao.findUserUid(EXT_USER_ID, SCHEME_NAME)).thenReturn(Optional.empty());

        assertThrows(AuthValidationException.class,
                () -> service.createSession(metaQuestRequest("scheme-user-4", EXT_USER_ID)));

        verify(sessionDao, never()).create(any());
        verify(userUidDao, never()).createUserUid(any());
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

        private final Set<String> linkedAccounts;

        TestModule() { this(Set.of()); }

        TestModule(Set<String> linkedAccounts) { this.linkedAccounts = linkedAccounts; }

        @Override
        protected void configure() {
            final var currentUser = new User();
            currentUser.setId(CURRENT_USER_ID);
            currentUser.setLinkedAccounts(linkedAccounts);
            bind(User.class).toInstance(currentUser);

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

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
        when(invoker.execute(any(), any(ResolvedRequest.class)))
                .thenReturn(new ParsedResponse(200, "{}", MAPPER.createObjectNode()));
    }

    /**
     * Anonymous user, uid/scheme not in db → creates a new user and a new UserUid pointing to them.
     */
    @Test
    public void testAnonymous_newUid_createsUserAndUid() {
        final var scheme = simpleScheme(SCHEME_NAME);
        when(schemeDao.getAuthScheme("scheme-anon-1")).thenReturn(scheme);
        when(userUidDao.findUserUid(EXT_USER_ID, SCHEME_NAME)).thenReturn(Optional.empty());

        final var newUser = userWithId("new-user-id");
        when(userDao.createUser(any(User.class))).thenReturn(newUser);

        service.createSession(simpleRequest("scheme-anon-1", EXT_USER_ID));

        verify(userDao).createUser(any(User.class));

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao).createUserUidStrict(uidCaptor.capture());
        assertEquals(uidCaptor.getValue().getId(), EXT_USER_ID);
        assertEquals(uidCaptor.getValue().getScheme(), SCHEME_NAME);
        assertEquals(uidCaptor.getValue().getUserId(), "new-user-id");
    }

    /**
     * Anonymous user, uid/scheme already in db → returns the existing user, no new user or uid created.
     */
    @Test
    public void testAnonymous_existingUid_returnsExistingUser() {
        final var scheme = simpleScheme(SCHEME_NAME);
        when(schemeDao.getAuthScheme("scheme-anon-2")).thenReturn(scheme);

        final var existingUser = userWithId("existing-user-id");
        when(userUidDao.findUserUid(EXT_USER_ID, SCHEME_NAME))
                .thenReturn(Optional.of(uidFor(EXT_USER_ID, SCHEME_NAME, "existing-user-id")));
        when(userDao.getUser("existing-user-id")).thenReturn(existingUser);

        service.createSession(simpleRequest("scheme-anon-2", EXT_USER_ID));

        verify(userDao, never()).createUser(any(User.class));
        verify(userUidDao, never()).createUserUidStrict(any(UserUid.class));

        final var sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getUser().getId(), "existing-user-id");
    }

    // ---------- helpers ----------

    private static OAuth2AuthScheme simpleScheme(String name) {
        final var s = new OAuth2AuthScheme();
        s.setId("ignored");
        s.setName(name);
        s.setValidationUrl("https://example.com/validate");
        s.setMethod(HttpMethod.POST);
        s.setBodyType(BodyType.FORM_URL_ENCODED);
        s.setHeaders(List.of());
        s.setParams(List.of());
        s.setBody(List.of(new OAuth2RequestKeyValue("user_id", null, true, true)));
        s.setValidStatusCodes(List.of(200));
        return s;
    }

    private static OAuth2SessionRequest simpleRequest(String schemeId, String userId) {
        final var r = new OAuth2SessionRequest();
        r.setSchemeId(schemeId);
        r.setRequestParameters(Map.of("user_id", userId));
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

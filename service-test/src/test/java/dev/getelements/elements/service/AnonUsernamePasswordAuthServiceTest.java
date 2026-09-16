package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.auth.CaptchaPublicConfiguration;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyRequest;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyResponse;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.CaptchaService;
import dev.getelements.elements.service.auth.AnonUsernamePasswordAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Validator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;

/**
 * Covers the new (Elements 3.9+) applicationNameOrId-based primary-profile resolution added to username/password
 * session creation, alongside the pre-existing profileId/profileSelector resolution it must not disturb.
 */
public class AnonUsernamePasswordAuthServiceTest {

    private static final String USER_ID = "test-user-id";

    @Inject private AnonUsernamePasswordAuthService service;
    @Inject private UserDao userDao;
    @Inject private ProfileDao profileDao;
    @Inject private ApplicationDao applicationDao;
    @Inject private SessionDao sessionDao;
    @Inject private ValidationHelper validationHelper;
    @Inject @Named(UNSCOPED) private CaptchaService captchaService;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
        when(sessionDao.create(any(Session.class))).thenReturn(new SessionCreation());

        final var captchaDisabled = new CaptchaPublicConfiguration();
        captchaDisabled.setEnabled(false);
        when(captchaService.getPublicConfiguration()).thenReturn(captchaDisabled);
    }

    @Test
    public void testSuperuserLoginRequiresCaptchaWhenEnabled() {

        final var user = userWithId(USER_ID);
        user.setLevel(SUPERUSER);
        when(userDao.validateUserPassword(anyString(), anyString())).thenReturn(user);

        final var captchaEnabled = new CaptchaPublicConfiguration();
        captchaEnabled.setEnabled(true);
        when(captchaService.getPublicConfiguration()).thenReturn(captchaEnabled);

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(USER_ID);
        request.setPassword("password");

        assertThrows(ForbiddenException.class, () -> service.createSession(request));
        verify(sessionDao, never()).create(any());

    }

    @Test
    public void testSuperuserLoginRejectedWhenCaptchaVerificationFails() {

        final var user = userWithId(USER_ID);
        user.setLevel(SUPERUSER);
        when(userDao.validateUserPassword(anyString(), anyString())).thenReturn(user);

        final var captchaEnabled = new CaptchaPublicConfiguration();
        captchaEnabled.setEnabled(true);
        when(captchaService.getPublicConfiguration()).thenReturn(captchaEnabled);

        final var failure = new CaptchaVerifyResponse();
        failure.setSuccess(false);
        when(captchaService.verify(any())).thenReturn(failure);

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(USER_ID);
        request.setPassword("password");
        request.setCaptchaToken("bad-token");

        assertThrows(ForbiddenException.class, () -> service.createSession(request));
        verify(sessionDao, never()).create(any());

    }

    @Test
    public void testSuperuserLoginSucceedsWithValidCaptcha() {

        final var user = userWithId(USER_ID);
        user.setLevel(SUPERUSER);
        when(userDao.validateUserPassword(anyString(), anyString())).thenReturn(user);

        final var captchaEnabled = new CaptchaPublicConfiguration();
        captchaEnabled.setEnabled(true);
        when(captchaService.getPublicConfiguration()).thenReturn(captchaEnabled);

        final var success = new CaptchaVerifyResponse();
        success.setSuccess(true);
        when(captchaService.verify(any())).thenReturn(success);

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(USER_ID);
        request.setPassword("password");
        request.setCaptchaToken("good-token");

        service.createSession(request);

        final var captor = org.mockito.ArgumentCaptor.forClass(CaptchaVerifyRequest.class);
        verify(captchaService).verify(captor.capture());
        assertEquals(captor.getValue().getToken(), "good-token");
        verify(sessionDao).create(any());

    }

    @Test
    public void testUserLoginNotGatedByCaptchaEvenWhenEnabled() {

        final var user = userWithId(USER_ID);
        user.setLevel(USER);
        when(userDao.validateUserPassword(anyString(), anyString())).thenReturn(user);

        final var captchaEnabled = new CaptchaPublicConfiguration();
        captchaEnabled.setEnabled(true);
        when(captchaService.getPublicConfiguration()).thenReturn(captchaEnabled);

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(USER_ID);
        request.setPassword("password");

        service.createSession(request);

        verify(captchaService, never()).verify(any());
        verify(sessionDao).create(any());

    }

    @Test
    public void testApplicationIdResolvesPrimaryProfileWhenNoProfileIdOrSelector() {

        final var user = userWithId(USER_ID);
        when(userDao.validateUserPassword(anyString(), anyString())).thenReturn(user);

        final var application = applicationWithId("app-1");
        when(applicationDao.findApplication("app-1")).thenReturn(Optional.of(application));

        final var primaryProfile = profileFor(user, application);
        when(profileDao.findPrimaryProfile(USER_ID, "app-1")).thenReturn(Optional.of(primaryProfile));

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(USER_ID);
        request.setPassword("password");
        request.setApplicationNameOrId("app-1");

        service.createSession(request);

        final var sessionCaptor = org.mockito.ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getProfile().getId(), primaryProfile.getId());
    }

    @Test
    public void testApplicationIdFallsBackToNoProfileWhenApplicationNotFound() {

        final var user = userWithId(USER_ID);
        when(userDao.validateUserPassword(anyString(), anyString())).thenReturn(user);
        when(applicationDao.findApplication("missing-app")).thenReturn(Optional.empty());

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(USER_ID);
        request.setPassword("password");
        request.setApplicationNameOrId("missing-app");

        service.createSession(request);

        final var sessionCaptor = org.mockito.ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertNull(sessionCaptor.getValue().getProfile());

        verify(profileDao, never()).findPrimaryProfile(any(), any());
    }

    @Test
    public void testExplicitProfileIdTakesPrecedenceOverApplicationId() {

        final var user = userWithId(USER_ID);
        when(userDao.validateUserPassword(anyString(), anyString())).thenReturn(user);

        final var explicitProfile = profileFor(user, applicationWithId("app-1"));
        explicitProfile.setId("explicit-profile-id");
        when(profileDao.getActiveProfile("explicit-profile-id")).thenReturn(explicitProfile);

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(USER_ID);
        request.setPassword("password");
        request.setProfileId("explicit-profile-id");
        request.setApplicationNameOrId("app-1");

        service.createSession(request);

        final var sessionCaptor = org.mockito.ArgumentCaptor.forClass(Session.class);
        verify(sessionDao).create(sessionCaptor.capture());
        assertEquals(sessionCaptor.getValue().getProfile().getId(), "explicit-profile-id");

        verify(applicationDao, never()).findApplication(any());
        verify(profileDao, never()).findPrimaryProfile(any(), any());
    }

    // ---------- helpers ----------

    private static User userWithId(final String id) {
        final var u = new User();
        u.setId(id);
        return u;
    }

    private static Application applicationWithId(final String id) {
        final var a = new Application();
        a.setId(id);
        return a;
    }

    private static Profile profileFor(final User user, final Application application) {
        final var p = new Profile();
        p.setId("profile-" + user.getId());
        p.setUser(user);
        p.setApplication(application);
        return p;
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserDao.class).toInstance(mock(UserDao.class));
            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(ApplicationDao.class).toInstance(mock(ApplicationDao.class));
            bind(SessionDao.class).toInstance(mock(SessionDao.class));
            bind(Validator.class).toInstance(mock(Validator.class));
            bind(ValidationHelper.class).toInstance(mock(ValidationHelper.class));
            bind(CaptchaService.class).annotatedWith(Names.named(UNSCOPED)).toInstance(mock(CaptchaService.class));
            bindConstant().annotatedWith(Names.named(SESSION_TIMEOUT_SECONDS)).to(3600L);
        }
    }

}

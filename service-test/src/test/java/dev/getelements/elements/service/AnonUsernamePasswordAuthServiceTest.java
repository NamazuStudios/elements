package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.TotpLoginChallengeDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.auth.TotpLoginChallenge;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.auth.MfaChallengeRequiredException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.MfaVerifyRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.TotpVerificationService;
import dev.getelements.elements.service.auth.AnonUsernamePasswordAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Validator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Optional;

import static com.google.inject.Guice.createInjector;
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
    @Inject @Named(UNSCOPED) private TotpVerificationService totpVerificationService;
    @Inject private TotpLoginChallengeDao totpLoginChallengeDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
        when(sessionDao.create(any(Session.class))).thenReturn(new SessionCreation());
        when(totpVerificationService.isRequiredFor(any())).thenReturn(false);
    }

    @Test
    public void testMfaChallengeRequiredWhenTotpEnrolled() {

        final var user = userWithId(USER_ID);
        when(userDao.validateUserPassword(anyString(), anyString())).thenReturn(user);
        when(totpVerificationService.isRequiredFor(user)).thenReturn(true);
        when(totpLoginChallengeDao.createChallenge(eq(USER_ID), any(), any(), any(), any()))
                .thenReturn("challenge-1");

        final var request = new UsernamePasswordSessionRequest();
        request.setUserId(USER_ID);
        request.setPassword("password");

        try {
            service.createSession(request);
            throw new AssertionError("Expected MfaChallengeRequiredException");
        } catch (MfaChallengeRequiredException e) {
            assertEquals(e.getChallengeId(), "challenge-1");
        }

        verify(sessionDao, never()).create(any());

    }

    @Test
    public void testCompleteMfaChallengeSucceedsWithValidCode() {

        final var user = userWithId(USER_ID);
        when(userDao.getUser(USER_ID)).thenReturn(user);

        final var challenge = new TotpLoginChallenge();
        challenge.setId("challenge-1");
        challenge.setUserId(USER_ID);
        when(totpLoginChallengeDao.find("challenge-1")).thenReturn(Optional.of(challenge));
        when(totpVerificationService.verify(user, "123456")).thenReturn(true);

        final var request = new MfaVerifyRequest();
        request.setChallengeId("challenge-1");
        request.setCode("123456");

        service.completeMfaChallenge(request);

        verify(sessionDao).create(any());

    }

    @Test
    public void testCompleteMfaChallengeRejectsInvalidCode() {

        final var user = userWithId(USER_ID);
        when(userDao.getUser(USER_ID)).thenReturn(user);

        final var challenge = new TotpLoginChallenge();
        challenge.setId("challenge-1");
        challenge.setUserId(USER_ID);
        when(totpLoginChallengeDao.find("challenge-1")).thenReturn(Optional.of(challenge));
        when(totpVerificationService.verify(user, "000000")).thenReturn(false);

        final var request = new MfaVerifyRequest();
        request.setChallengeId("challenge-1");
        request.setCode("000000");

        assertThrows(ForbiddenException.class, () -> service.completeMfaChallenge(request));
        verify(sessionDao, never()).create(any());

        // A wrong guess must not burn the challenge -- the DAO's consume() must never be called on failure,
        // so a legitimate follow-up attempt (e.g. falling back to a recovery code) can still use it.
        verify(totpLoginChallengeDao, never()).consume(any());

    }

    @Test
    public void testCompleteMfaChallengeRetrySucceedsAfterAFailedAttempt() {

        final var user = userWithId(USER_ID);
        when(userDao.getUser(USER_ID)).thenReturn(user);

        final var challenge = new TotpLoginChallenge();
        challenge.setId("challenge-1");
        challenge.setUserId(USER_ID);
        when(totpLoginChallengeDao.find("challenge-1")).thenReturn(Optional.of(challenge));
        when(totpVerificationService.verify(user, "000000")).thenReturn(false);
        when(totpVerificationService.verify(user, "recovery-code-1")).thenReturn(true);

        final var badAttempt = new MfaVerifyRequest();
        badAttempt.setChallengeId("challenge-1");
        badAttempt.setCode("000000");
        assertThrows(ForbiddenException.class, () -> service.completeMfaChallenge(badAttempt));

        // Same challenge ID, now with a recovery code -- must still be usable since the first attempt failed.
        final var retryAttempt = new MfaVerifyRequest();
        retryAttempt.setChallengeId("challenge-1");
        retryAttempt.setCode("recovery-code-1");
        service.completeMfaChallenge(retryAttempt);

        verify(sessionDao).create(any());
        verify(totpLoginChallengeDao).consume("challenge-1");

    }

    @Test
    public void testCompleteMfaChallengeRejectsUnknownChallenge() {

        when(totpLoginChallengeDao.find("missing")).thenReturn(Optional.empty());

        final var request = new MfaVerifyRequest();
        request.setChallengeId("missing");
        request.setCode("123456");

        assertThrows(NotFoundException.class, () -> service.completeMfaChallenge(request));
        verify(sessionDao, never()).create(any());

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
            bind(TotpVerificationService.class)
                    .annotatedWith(Names.named(UNSCOPED))
                    .toInstance(mock(TotpVerificationService.class));
            bind(TotpLoginChallengeDao.class).toInstance(mock(TotpLoginChallengeDao.class));
            bindConstant().annotatedWith(Names.named(SESSION_TIMEOUT_SECONDS)).to(3600L);
        }
    }

}

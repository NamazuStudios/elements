package dev.getelements.elements.service.user;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.PasswordResetTokenDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.user.PasswordResetToken;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.email.EmailService;
import jakarta.inject.Inject;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import static dev.getelements.elements.sdk.service.user.PasswordResetService.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class PasswordResetServiceTest {

    private static final String USER_ID      = "user-id-abc123";
    private static final String BASE_URL     = "https://example.com/reset";
    private static final String TEST_TOKEN   = "aaaa-bbbb-cccc-dddd-eeee";
    private static final String SUBJECT      = "Reset your password";
    private static final String TEMPLATE     = "<a href=\"{link}\">Reset</a>";
    private static final String EXPIRY_HOURS = "1";

    // Distinct emails per test to avoid static cooldown map interference
    private static final String EMAIL_HAPPY = "happy@example.com";
    private static final String EMAIL_NORM  = "norm@example.com";      // normalised form
    private static final String EMAIL_LINK  = "link@example.com";
    private static final String EMAIL_COOL  = "cooldown@example.com";

    @Inject private AnonPasswordResetService service;
    @Inject private UserUidDao userUidDao;
    @Inject private UserDao userDao;
    @Inject private PasswordResetTokenDao tokenDao;
    @Inject private EmailService emailService;
    @Inject private ElementRegistry elementRegistry;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
    }

    // ---------- requestReset ----------

    /**
     * Happy path: email is registered → token created, email sent, event published.
     */
    @Test
    public void requestReset_knownEmail_createsTokenAndSendsEmail() {
        when(userUidDao.findUserUid(EMAIL_HAPPY, SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor(EMAIL_HAPPY, USER_ID)));
        when(userDao.getUser(USER_ID)).thenReturn(userWith(USER_ID));
        when(tokenDao.createToken(any(User.class), any(Timestamp.class))).thenReturn(TEST_TOKEN);

        service.requestReset(EMAIL_HAPPY, BASE_URL);

        verify(tokenDao).createToken(any(User.class), any(Timestamp.class));
        verify(emailService).send(isNull(), eq(EMAIL_HAPPY), eq(SUBJECT), anyString(), eq(true));
        final var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(elementRegistry).publish(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().getEventName(), PASSWORD_RESET_REQUESTED_EVENT);
    }

    /**
     * Unknown email → returns silently; no token created, no email sent (no user enumeration).
     */
    @Test
    public void requestReset_unknownEmail_returnsSilently() {
        when(userUidDao.findUserUid(any(), eq(SCHEME_EMAIL))).thenReturn(Optional.empty());

        service.requestReset("nobody@example.com", BASE_URL);

        verify(tokenDao, never()).createToken(any(), any());
        verify(emailService, never()).send(any(), any(), any(), any(), anyBoolean());
        verify(elementRegistry, never()).publish(any());
    }

    /**
     * Email with mixed case and whitespace → normalised to lowercase before lookup and delivery.
     */
    @Test
    public void requestReset_emailNormalized() {
        when(userUidDao.findUserUid(EMAIL_NORM, SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor(EMAIL_NORM, USER_ID)));
        when(userDao.getUser(USER_ID)).thenReturn(userWith(USER_ID));
        when(tokenDao.createToken(any(User.class), any(Timestamp.class))).thenReturn(TEST_TOKEN);

        service.requestReset("  NORM@EXAMPLE.COM  ", BASE_URL);

        verify(userUidDao).findUserUid(EMAIL_NORM, SCHEME_EMAIL);
        verify(emailService).send(isNull(), eq(EMAIL_NORM), any(), any(), anyBoolean());
    }

    /**
     * The {link} placeholder in the email template is replaced with baseUrl?token=<token>.
     */
    @Test
    public void requestReset_templateLinkSubstituted() {
        when(userUidDao.findUserUid(EMAIL_LINK, SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor(EMAIL_LINK, USER_ID)));
        when(userDao.getUser(USER_ID)).thenReturn(userWith(USER_ID));
        when(tokenDao.createToken(any(User.class), any(Timestamp.class))).thenReturn(TEST_TOKEN);

        service.requestReset(EMAIL_LINK, BASE_URL);

        final var bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(any(), any(), any(), bodyCaptor.capture(), anyBoolean());
        final var body = bodyCaptor.getValue();
        assertTrue(body.contains(BASE_URL + "?token=" + TEST_TOKEN),
                "Expected full reset link in body, got: " + body);
        assertFalse(body.contains("{link}"), "{link} placeholder was not replaced");
    }

    /**
     * Two requests for the same email within 60 seconds: only the first results in a token and email.
     */
    @Test
    public void requestReset_cooldownSuppressesDuplicate() {
        when(userUidDao.findUserUid(EMAIL_COOL, SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor(EMAIL_COOL, USER_ID)));
        when(userDao.getUser(USER_ID)).thenReturn(userWith(USER_ID));
        when(tokenDao.createToken(any(User.class), any(Timestamp.class))).thenReturn(TEST_TOKEN);

        service.requestReset(EMAIL_COOL, BASE_URL);
        service.requestReset(EMAIL_COOL, BASE_URL);

        verify(tokenDao, times(1)).createToken(any(), any());
        verify(emailService, times(1)).send(any(), any(), any(), any(), anyBoolean());
    }

    // ---------- completeReset ----------

    /**
     * Valid token → password updated and token deleted (single-use).
     */
    @Test
    public void completeReset_validToken_setsPasswordAndDeletesToken() {
        when(tokenDao.findToken(TEST_TOKEN)).thenReturn(Optional.of(tokenFor(USER_ID)));

        service.completeReset(TEST_TOKEN, "newpassword");

        verify(userDao).setPassword(USER_ID, "newpassword");
        verify(tokenDao).deleteToken(TEST_TOKEN);
    }

    /**
     * Valid token → PASSWORD_RESET_COMPLETED_EVENT is published.
     */
    @Test
    public void completeReset_validToken_publishesCompletedEvent() {
        when(tokenDao.findToken(TEST_TOKEN)).thenReturn(Optional.of(tokenFor(USER_ID)));

        service.completeReset(TEST_TOKEN, "newpassword");

        final var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(elementRegistry).publish(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().getEventName(), PASSWORD_RESET_COMPLETED_EVENT);
    }

    /**
     * setPassword is called before deleteToken — if password update fails the token is not consumed.
     */
    @Test
    public void completeReset_setPasswordCalledBeforeDeleteToken() {
        when(tokenDao.findToken(TEST_TOKEN)).thenReturn(Optional.of(tokenFor(USER_ID)));
        final InOrder order = inOrder(userDao, tokenDao);

        service.completeReset(TEST_TOKEN, "newpassword");

        order.verify(userDao).setPassword(USER_ID, "newpassword");
        order.verify(tokenDao).deleteToken(TEST_TOKEN);
    }

    /**
     * Unknown or expired token → InvalidParameterException; password and token are untouched.
     */
    @Test(expectedExceptions = InvalidParameterException.class)
    public void completeReset_invalidToken_throwsInvalidParameter() {
        when(tokenDao.findToken(any())).thenReturn(Optional.empty());

        service.completeReset("no-such-token", "newpassword");
    }

    @Test
    public void completeReset_invalidToken_doesNotTouchPasswordOrToken() {
        when(tokenDao.findToken(any())).thenReturn(Optional.empty());

        try {
            service.completeReset("no-such-token", "newpassword");
        } catch (InvalidParameterException ignored) {}

        verify(userDao, never()).setPassword(any(), any());
        verify(tokenDao, never()).deleteToken(any());
    }

    // ---------- helpers ----------

    private static UserUid uidFor(final String email, final String userId) {
        final var uid = new UserUid();
        uid.setId(email);
        uid.setScheme(SCHEME_EMAIL);
        uid.setUserId(userId);
        return uid;
    }

    private static User userWith(final String id) {
        final var user = new User();
        user.setId(id);
        return user;
    }

    private static PasswordResetToken tokenFor(final String userId) {
        final var token = new PasswordResetToken();
        token.setId(TEST_TOKEN);
        token.setUser(userWith(userId));
        return token;
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserUidDao.class).toInstance(mock(UserUidDao.class));
            bind(UserDao.class).toInstance(mock(UserDao.class));
            bind(PasswordResetTokenDao.class).toInstance(mock(PasswordResetTokenDao.class));
            bind(EmailService.class).toInstance(mock(EmailService.class));
            bind(ElementRegistry.class).toInstance(mock(ElementRegistry.class));

            bindConstant().annotatedWith(named(RESET_EMAIL_SUBJECT)).to(SUBJECT);
            bindConstant().annotatedWith(named(RESET_EMAIL_TEMPLATE)).to(TEMPLATE);
            bindConstant().annotatedWith(named(RESET_TOKEN_EXPIRY_HOURS)).to(EXPIRY_HOURS);
        }
    }

}

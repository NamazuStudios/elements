package dev.getelements.elements.service.user;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.UidVerificationTokenDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.UidVerificationToken;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.service.email.EmailService;
import jakarta.inject.Inject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import static dev.getelements.elements.sdk.service.user.EmailVerificationService.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class EmailVerificationServiceTest {

    private static final String CURRENT_USER_ID = "current-user-id";
    private static final String OTHER_USER_ID   = "other-user-id";
    private static final String EMAIL           = "test@example.com";
    private static final String BASE_URL        = "https://example.com/api/rest/verify";
    private static final String TEST_TOKEN      = "aabbccdd-1122-3344-5566-aabbccddeeff";
    private static final String SUBJECT         = "Verify your email";
    private static final String TEMPLATE        = "<a href=\"{link}\">Verify</a>";

    @Inject private UserEmailVerificationService userService;
    @Inject private SuperUserEmailVerificationService superUserService;
    @Inject private UserUidDao userUidDao;
    @Inject private UidVerificationTokenDao tokenDao;
    @Inject private EmailService emailService;
    @Inject private ElementRegistry elementRegistry;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
    }

    // ---------- UserEmailVerificationService: requestVerification ----------

    /**
     * Happy path: UID already exists and belongs to the current user → email sent, status set to
     * PENDING, event published.
     */
    @Test
    public void requestVerification_ownEmail_movesPendingAndSendsEmail() {
        when(userUidDao.findUserUid(EMAIL, SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor(EMAIL, SCHEME_EMAIL, CURRENT_USER_ID)));
        when(tokenDao.createToken(any(), eq(SCHEME_EMAIL), eq(EMAIL), any(Timestamp.class)))
                .thenReturn(TEST_TOKEN);
        when(userUidDao.updateVerificationStatus(EMAIL, SCHEME_EMAIL, VerificationStatus.PENDING))
                .thenReturn(uidWithStatus(VerificationStatus.PENDING));

        final var result = userService.requestVerification(EMAIL, BASE_URL);

        assertEquals(result.getVerificationStatus(), VerificationStatus.PENDING);
        verify(emailService).send(isNull(), eq(EMAIL), eq(SUBJECT), anyString(), eq(true));
        final var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(elementRegistry).publish(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().getEventName(), EMAIL_VERIFICATION_REQUESTED_EVENT);
    }

    /**
     * Email UID is already VERIFIED (e.g. set by OIDC login) → verification is still triggered.
     * This ensures a user who signed up via OIDC must go through our own email flow before they
     * can complete an email+password link, even though the OIDC provider already verified the
     * address.  No early-return; status transitions VERIFIED → PENDING.
     */
    @Test
    public void requestVerification_alreadyVerified_reVerifiesViaEmail() {
        final var verifiedUid = uidForWithStatus(EMAIL, SCHEME_EMAIL, CURRENT_USER_ID, VerificationStatus.VERIFIED);
        when(userUidDao.findUserUid(EMAIL, SCHEME_EMAIL)).thenReturn(Optional.of(verifiedUid));
        when(tokenDao.createToken(any(), eq(SCHEME_EMAIL), eq(EMAIL), any(Timestamp.class)))
                .thenReturn(TEST_TOKEN);
        when(userUidDao.updateVerificationStatus(EMAIL, SCHEME_EMAIL, VerificationStatus.PENDING))
                .thenReturn(uidWithStatus(VerificationStatus.PENDING));

        final var result = userService.requestVerification(EMAIL, BASE_URL);

        assertEquals(result.getVerificationStatus(), VerificationStatus.PENDING);
        verify(emailService).send(isNull(), eq(EMAIL), eq(SUBJECT), anyString(), eq(true));
        verify(userUidDao).updateVerificationStatus(EMAIL, SCHEME_EMAIL, VerificationStatus.PENDING);
        final var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(elementRegistry).publish(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().getEventName(), EMAIL_VERIFICATION_REQUESTED_EVENT);
    }

    /**
     * Email UID does not exist yet → UID created with UNVERIFIED status, then email sent and
     * status advanced to PENDING.  Two events are published: USER_UID_CREATED and
     * EMAIL_VERIFICATION_REQUESTED.
     */
    @Test
    public void requestVerification_newEmail_createsUidThenMovesToPending() {
        when(userUidDao.findUserUid(EMAIL, SCHEME_EMAIL)).thenReturn(Optional.empty());
        when(userUidDao.createUserUidStrict(any())).then(i -> i.getArgument(0));
        when(tokenDao.createToken(any(), eq(SCHEME_EMAIL), eq(EMAIL), any(Timestamp.class)))
                .thenReturn(TEST_TOKEN);
        when(userUidDao.updateVerificationStatus(EMAIL, SCHEME_EMAIL, VerificationStatus.PENDING))
                .thenReturn(uidWithStatus(VerificationStatus.PENDING));

        final var result = userService.requestVerification(EMAIL, BASE_URL);

        assertEquals(result.getVerificationStatus(), VerificationStatus.PENDING);

        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao).createUserUidStrict(uidCaptor.capture());
        final var created = uidCaptor.getValue();
        assertEquals(created.getId(), EMAIL);
        assertEquals(created.getScheme(), SCHEME_EMAIL);
        assertEquals(created.getUserId(), CURRENT_USER_ID);
        assertEquals(created.getVerificationStatus(), VerificationStatus.UNVERIFIED);

        verify(emailService).send(isNull(), eq(EMAIL), eq(SUBJECT), anyString(), eq(true));

        final var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(elementRegistry, times(2)).publish(eventCaptor.capture());
        final var eventNames = eventCaptor.getAllValues().stream()
                .map(Event::getEventName)
                .toList();
        assertTrue(eventNames.contains(UserUid.USER_UID_CREATED_EVENT));
        assertTrue(eventNames.contains(EMAIL_VERIFICATION_REQUESTED_EVENT));
    }

    /**
     * Email supplied with mixed case and leading/trailing whitespace → normalized to lowercase
     * before lookup and UID creation, so it matches the canonical stored form.
     */
    @Test
    public void requestVerification_emailNormalized() {
        // Stored as canonical lowercase
        when(userUidDao.findUserUid("foo@example.com", SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor("foo@example.com", SCHEME_EMAIL, CURRENT_USER_ID)));
        when(tokenDao.createToken(any(), eq(SCHEME_EMAIL), eq("foo@example.com"), any(Timestamp.class)))
                .thenReturn(TEST_TOKEN);
        when(userUidDao.updateVerificationStatus("foo@example.com", SCHEME_EMAIL, VerificationStatus.PENDING))
                .thenReturn(uidWithStatus(VerificationStatus.PENDING));

        // Caller supplies mixed-case with whitespace
        userService.requestVerification("  FOO@EXAMPLE.COM  ", BASE_URL);

        verify(userUidDao).findUserUid("foo@example.com", SCHEME_EMAIL);
        verify(emailService).send(isNull(), eq("foo@example.com"), eq(SUBJECT), anyString(), eq(true));
    }

    /**
     * UID belongs to a different user → ForbiddenException; no email sent.
     */
    @Test(expectedExceptions = ForbiddenException.class)
    public void requestVerification_otherUsersEmail_throwsForbidden() {
        when(userUidDao.findUserUid(EMAIL, SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor(EMAIL, SCHEME_EMAIL, OTHER_USER_ID)));

        userService.requestVerification(EMAIL, BASE_URL);

        verify(emailService, never()).send(any(), any(), any(), any(), anyBoolean());
    }

    /**
     * The {link} placeholder in the email template is replaced with baseUrl?token=<token>.
     */
    @Test
    public void requestVerification_templateLinkSubstituted() {
        when(userUidDao.findUserUid(EMAIL, SCHEME_EMAIL))
                .thenReturn(Optional.of(uidFor(EMAIL, SCHEME_EMAIL, CURRENT_USER_ID)));
        when(tokenDao.createToken(any(), eq(SCHEME_EMAIL), eq(EMAIL), any(Timestamp.class)))
                .thenReturn(TEST_TOKEN);
        when(userUidDao.updateVerificationStatus(EMAIL, SCHEME_EMAIL, VerificationStatus.PENDING))
                .thenReturn(uidWithStatus(VerificationStatus.PENDING));

        userService.requestVerification(EMAIL, BASE_URL);

        final var bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(any(), any(), any(), bodyCaptor.capture(), anyBoolean());
        final var body = bodyCaptor.getValue();
        assertTrue(body.contains(BASE_URL + "?token=" + TEST_TOKEN),
                "Expected full verification link in body, got: " + body);
        assertFalse(body.contains("{link}"), "{link} placeholder was not replaced");
    }

    // ---------- UserEmailVerificationService: completeVerification ----------

    /**
     * Valid token → status set to VERIFIED, token deleted (single-use), event published.
     */
    @Test
    public void completeVerification_validToken_movesVerifiedAndDeletesToken() {
        when(tokenDao.findToken(TEST_TOKEN)).thenReturn(Optional.of(tokenWith(EMAIL, SCHEME_EMAIL)));
        when(userUidDao.updateVerificationStatus(EMAIL, SCHEME_EMAIL, VerificationStatus.VERIFIED))
                .thenReturn(uidWithStatus(VerificationStatus.VERIFIED));

        final var result = userService.completeVerification(TEST_TOKEN);

        assertEquals(result.getVerificationStatus(), VerificationStatus.VERIFIED);
        verify(tokenDao).deleteToken(TEST_TOKEN);
        final var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(elementRegistry).publish(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().getEventName(), EMAIL_VERIFICATION_COMPLETED_EVENT);
    }

    /**
     * Token not found (absent or expired) → NotFoundException.
     */
    @Test(expectedExceptions = NotFoundException.class)
    public void completeVerification_tokenNotFound_throwsNotFound() {
        when(tokenDao.findToken(TEST_TOKEN)).thenReturn(Optional.empty());

        userService.completeVerification(TEST_TOKEN);
    }

    // ---------- SuperUserEmailVerificationService tests ----------

    /**
     * Superuser: requestVerification succeeds even when the UID belongs to a different user.
     */
    @Test
    public void superUser_requestVerification_noOwnershipCheck() {
        when(userUidDao.getUserUid(EMAIL, SCHEME_EMAIL))
                .thenReturn(uidFor(EMAIL, SCHEME_EMAIL, OTHER_USER_ID));
        when(tokenDao.createToken(any(), eq(SCHEME_EMAIL), eq(EMAIL), any(Timestamp.class)))
                .thenReturn(TEST_TOKEN);
        when(userUidDao.updateVerificationStatus(EMAIL, SCHEME_EMAIL, VerificationStatus.PENDING))
                .thenReturn(uidWithStatus(VerificationStatus.PENDING));

        final var result = superUserService.requestVerification(EMAIL, BASE_URL);

        assertNotNull(result);
        verify(emailService).send(any(), eq(EMAIL), any(), any(), anyBoolean());
    }

    /**
     * Superuser: completeVerification with valid token succeeds.
     */
    @Test
    public void superUser_completeVerification_validToken_returnsVerified() {
        when(tokenDao.findToken(TEST_TOKEN)).thenReturn(Optional.of(tokenWith(EMAIL, SCHEME_EMAIL)));
        when(userUidDao.updateVerificationStatus(EMAIL, SCHEME_EMAIL, VerificationStatus.VERIFIED))
                .thenReturn(uidWithStatus(VerificationStatus.VERIFIED));

        final var result = superUserService.completeVerification(TEST_TOKEN);

        assertEquals(result.getVerificationStatus(), VerificationStatus.VERIFIED);
        verify(tokenDao).deleteToken(TEST_TOKEN);
    }

    // ---------- helpers ----------

    private static UserUid uidFor(final String id, final String scheme, final String userId) {
        return uidForWithStatus(id, scheme, userId, null);
    }

    private static UserUid uidForWithStatus(final String id, final String scheme,
                                             final String userId, final VerificationStatus status) {
        final var uid = new UserUid();
        uid.setId(id);
        uid.setScheme(scheme);
        uid.setUserId(userId);
        uid.setVerificationStatus(status);
        return uid;
    }

    private static UserUid uidWithStatus(final VerificationStatus status) {
        final var uid = new UserUid();
        uid.setVerificationStatus(status);
        return uid;
    }

    private static UidVerificationToken tokenWith(final String uidId, final String scheme) {
        final var t = new UidVerificationToken();
        t.setToken(TEST_TOKEN);
        t.setUidId(uidId);
        t.setScheme(scheme);
        return t;
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            final var currentUser = new User();
            currentUser.setId(CURRENT_USER_ID);
            bind(User.class).toInstance(currentUser);

            bind(UserUidDao.class).toInstance(mock(UserUidDao.class));
            bind(UidVerificationTokenDao.class).toInstance(mock(UidVerificationTokenDao.class));
            bind(EmailService.class).toInstance(mock(EmailService.class));
            bind(ElementRegistry.class).toInstance(mock(ElementRegistry.class));

            bindConstant().annotatedWith(named(VERIFICATION_EMAIL_SUBJECT)).to(SUBJECT);
            bindConstant().annotatedWith(named(VERIFICATION_EMAIL_TEMPLATE)).to(TEMPLATE);
        }
    }

}

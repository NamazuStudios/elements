package dev.getelements.elements.service.invite;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.SuperUserInviteTokenDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import dev.getelements.elements.sdk.model.invite.CreateSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.sdk.model.invite.RedeemSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.schema.email.EmailTemplate;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.service.email.EmailService;
import dev.getelements.elements.sdk.service.schema.email.EmailTemplateService;
import jakarta.inject.Inject;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import dev.getelements.elements.sdk.service.invite.InviteService;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static dev.getelements.elements.sdk.service.invite.InviteService.SUPERUSER_INVITE_BASE_URL;
import static dev.getelements.elements.sdk.service.invite.InviteService.SUPERUSER_INVITE_EXPIRY_HOURS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Exhaustive unit coverage for the superuser email-invite flow (#91): issuance (normalization,
 * duplicate guard, expiry, email templating, event publication), redemption (email binding,
 * verification status, level elevation, single-use semantics), revocation, listing, and the
 * access-level guards distinguishing {@link UserInviteService} from {@link SuperUserInviteService}.
 */
public class SuperUserInviteServiceTest {

    private static final String INVITEE_ID = "invitee-1";
    private static final String EMAIL = "invitee@example.com";
    private static final String EMAIL_MIXED = "  Invitee@Example.COM  ";
    private static final String TOKEN = "aaaa-bbbb-cccc-dddd-eeee";
    private static final String BASE_URL = "https://admin.example.com/invite";
    private static final String EXPIRY_HOURS = "24";
    private static final String SUBJECT = "Superuser Invitation";
    private static final String TEMPLATE = "<a href=\"{link}\">Accept</a>";

    @Inject private SuperUserInviteService superUserService;
    @Inject private UserInviteService userInviteService;

    private SuperUserInviteTokenDao inviteTokenDao;
    private UserDao userDao;
    private UserUidDao userUidDao;
    private dev.getelements.elements.sdk.dao.ProfileDao profileDao;
    private EmailService emailService;
    private EmailTemplateService emailTemplateService;
    private ElementRegistry elementRegistry;

    private User currentUser;

    @BeforeMethod
    public void setup() {
        inviteTokenDao = mock(SuperUserInviteTokenDao.class);
        userDao = mock(UserDao.class);
        userUidDao = mock(UserUidDao.class);
        profileDao = mock(dev.getelements.elements.sdk.dao.ProfileDao.class);
        emailService = mock(EmailService.class);
        emailTemplateService = mock(EmailTemplateService.class);
        elementRegistry = mock(ElementRegistry.class);

        currentUser = userWith(INVITEE_ID, User.Level.USER);

        final var template = new EmailTemplate();
        template.setSubject(SUBJECT);
        template.setBody(TEMPLATE);
        when(emailTemplateService.getOrCreateEmailTemplate(any(), any(), any(), any())).thenReturn(template);

        reinject(BASE_URL);
    }

    /**
     * Re-creates the injector, binding the current {@link #currentUser} and base URL. Services hold
     * whatever is bound at injection time, so tests that need a different current user or base URL
     * re-inject rather than mutate post-injection.
     */
    private void reinject(final String baseUrl) {
        createInjector(new TestModule(baseUrl)).injectMembers(this);
    }

    // =========================================================================
    // Issuance (superuser-only)
    // =========================================================================

    @Test
    public void issue_withValidEmail_createsInviteAndSendsEmail() {
        asSuperuser();
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        final var invite = superUserService.createSuperUserInvite(createRequest(EMAIL));

        assertNotNull(invite);
        verify(inviteTokenDao).createInvite(eq(currentUser), eq(EMAIL), any(Timestamp.class), any(Timestamp.class));
        verify(emailService).send(isNull(), eq(EMAIL), eq(SUBJECT), anyString(), eq(true));
    }

    @Test
    public void issue_emailNormalized_beforeCreateAndSend() {
        asSuperuser();
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        superUserService.createSuperUserInvite(createRequest(EMAIL_MIXED));

        verify(inviteTokenDao).findPendingInviteByEmail(EMAIL);
        verify(inviteTokenDao).createInvite(any(), eq(EMAIL), any(), any());
        verify(emailService).send(isNull(), eq(EMAIL), any(), any(), anyBoolean());
    }

    @Test
    public void issue_recordsIssuerAsCurrentUser() {
        asSuperuser();
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        superUserService.createSuperUserInvite(createRequest(EMAIL));

        final var issuerCaptor = ArgumentCaptor.forClass(User.class);
        verify(inviteTokenDao).createInvite(issuerCaptor.capture(), any(), any(), any());
        assertEquals(issuerCaptor.getValue().getId(), INVITEE_ID);
        assertEquals(issuerCaptor.getValue().getLevel(), User.Level.SUPERUSER);
    }

    @Test
    public void issue_defaultExpiryIs24Hours() {
        asSuperuser();
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        final var before = System.currentTimeMillis();
        superUserService.createSuperUserInvite(createRequest(EMAIL));
        final var after = System.currentTimeMillis();

        final var expiryCaptor = ArgumentCaptor.forClass(Timestamp.class);
        verify(inviteTokenDao).createInvite(any(), any(), expiryCaptor.capture(), any());

        final var expiry = expiryCaptor.getValue().getTime();
        assertTrue(expiry >= before + TimeUnit.HOURS.toMillis(24) - 1000, "expiry should be ~24h out");
        assertTrue(expiry <= after + TimeUnit.HOURS.toMillis(24) + 1000, "expiry should be ~24h out");
    }

    @Test
    public void issue_customExpiryHours_honored() {
        asSuperuser();
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        final var request = createRequest(EMAIL);
        request.setExpiryHours(48);

        superUserService.createSuperUserInvite(request);

        final var expiryCaptor = ArgumentCaptor.forClass(Timestamp.class);
        verify(inviteTokenDao).createInvite(any(), any(), expiryCaptor.capture(), any());

        final var delta = expiryCaptor.getValue().getTime() - System.currentTimeMillis();
        assertTrue(delta > TimeUnit.HOURS.toMillis(47), "expiry should be ~48h out, was " + delta);
    }

    @Test
    public void issue_blankEmail_throwsInvalidParameter() {
        asSuperuser();

        assertThrows(InvalidParameterException.class, () -> superUserService.createSuperUserInvite(createRequest("   ")));

        verify(inviteTokenDao, never()).createInvite(any(), any(), any(), any());
        verify(emailService, never()).send(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    public void issue_duplicatePendingInvite_throwsInvalidParameter() {
        asSuperuser();
        when(inviteTokenDao.findPendingInviteByEmail(EMAIL))
                .thenReturn(Optional.of(inviteFor("existing-token", EMAIL, SuperUserInvite.Status.PENDING)));

        assertThrows(InvalidParameterException.class, () -> superUserService.createSuperUserInvite(createRequest(EMAIL)));

        verify(inviteTokenDao, never()).createInvite(any(), any(), any(), any());
        verify(emailService, never()).send(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    public void issue_expiredPriorInvite_doesNotBlockNewInvite() {
        asSuperuser();
        // findPendingInviteByEmail only surfaces pending, unexpired invites, so an expired invite
        // for the same email does not block re-issuance.
        when(inviteTokenDao.findPendingInviteByEmail(EMAIL)).thenReturn(Optional.empty());
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        superUserService.createSuperUserInvite(createRequest(EMAIL));

        verify(inviteTokenDao).createInvite(any(), any(), any(), any());
    }

    @Test
    public void issue_templateLinkSubstituted() {
        asSuperuser();
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        superUserService.createSuperUserInvite(createRequest(EMAIL));

        final var bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(any(), any(), any(), bodyCaptor.capture(), anyBoolean());

        final var body = bodyCaptor.getValue();
        assertTrue(body.contains(BASE_URL + "?token=" + TOKEN), "Expected full invite link in body: " + body);
        assertTrue(!body.contains("{link}"), "{link} placeholder was not replaced");
    }

    @Test
    public void issue_baseUrlWithTrailingSlash_linkWellFormed() {
        asSuperuser();
        reinject(BASE_URL + "/");
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        superUserService.createSuperUserInvite(createRequest(EMAIL));

        final var bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(any(), any(), any(), bodyCaptor.capture(), anyBoolean());
        assertTrue(bodyCaptor.getValue().contains(BASE_URL + "?token=" + TOKEN),
                "Trailing slash must be collapsed: " + bodyCaptor.getValue());
    }

    @Test
    public void issue_blankBaseUrl_createsInviteWithoutEmail() {
        asSuperuser();
        reinject("  ");
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        superUserService.createSuperUserInvite(createRequest(EMAIL));

        verify(inviteTokenDao).createInvite(any(), any(), any(), any());
        verify(emailService, never()).send(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    public void issue_publishesCreatedEvent() {
        asSuperuser();
        when(inviteTokenDao.createInvite(any(), any(), any(), any())).thenReturn(TOKEN);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        superUserService.createSuperUserInvite(createRequest(EMAIL));

        final var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(elementRegistry).publish(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().getEventName(), AbstractSuperUserInviteService.INVITE_CREATED_EVENT);
    }

    @Test
    public void issue_asUser_forbidden() {
        asUser();

        assertThrows(ForbiddenException.class, () -> userInviteService.createSuperUserInvite(createRequest(EMAIL)));

        verify(inviteTokenDao, never()).createInvite(any(), any(), any(), any());
        verify(emailService, never()).send(any(), any(), any(), any(), anyBoolean());
    }

    // =========================================================================
    // Redemption (user-level allowed; that is the whole point)
    // =========================================================================

    @Test
    public void redeem_validToken_verifiedMatchingEmail_elevatesToSuperuser() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID))
                .thenReturn(List.of(emailUid(EMAIL, VerificationStatus.VERIFIED)));
        when(userDao.getUser(INVITEE_ID)).thenReturn(userWith(INVITEE_ID, User.Level.USER));
        when(inviteTokenDao.redeemToken(eq(TOKEN), any())).thenReturn(true);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.REDEEMED)));

        final var result = userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN));

        assertNotNull(result);
        final var elevatedCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).updateUser(elevatedCaptor.capture());
        assertEquals(elevatedCaptor.getValue().getLevel(), User.Level.SUPERUSER);
        verify(inviteTokenDao).redeemToken(eq(TOKEN), any());
    }

    @Test
    public void redeem_publishesRedeemedEvent() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID))
                .thenReturn(List.of(emailUid(EMAIL, VerificationStatus.VERIFIED)));
        when(userDao.getUser(INVITEE_ID)).thenReturn(userWith(INVITEE_ID, User.Level.USER));
        when(inviteTokenDao.redeemToken(eq(TOKEN), any())).thenReturn(true);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.REDEEMED)));

        userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN));

        final var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(elementRegistry).publish(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().getEventName(), AbstractSuperUserInviteService.INVITE_REDEEMED_EVENT);
    }

    @Test
    public void redeem_redeemsBeforeElevating() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID))
                .thenReturn(List.of(emailUid(EMAIL, VerificationStatus.VERIFIED)));
        when(userDao.getUser(INVITEE_ID)).thenReturn(userWith(INVITEE_ID, User.Level.USER));
        when(inviteTokenDao.redeemToken(eq(TOKEN), any())).thenReturn(true);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.REDEEMED)));

        userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN));

        // Redemption must be recorded before the elevation: if elevation subsequently fails, the
        // invite is already consumed and a fresh one must be issued rather than silently retried.
        final InOrder order = inOrder(inviteTokenDao, userDao);
        order.verify(inviteTokenDao).redeemToken(eq(TOKEN), any());
        order.verify(userDao).updateUser(any());
    }

    @Test
    public void redeem_elevationUsesFreshUserRecord() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID))
                .thenReturn(List.of(emailUid(EMAIL, VerificationStatus.VERIFIED)));
        when(userDao.getUser(INVITEE_ID)).thenReturn(userWith(INVITEE_ID, User.Level.USER));
        when(inviteTokenDao.redeemToken(eq(TOKEN), any())).thenReturn(true);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.REDEEMED)));

        userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN));

        // The injected current user may be a request-scoped detached copy; elevation must
        // round-trip through the DAO so no other fields are clobbered.
        verify(userDao).getUser(INVITEE_ID);
    }

    @Test
    public void redeem_emailMatchIsCaseInsensitive() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID))
                .thenReturn(List.of(emailUid("INVITEE@EXAMPLE.COM", VerificationStatus.VERIFIED)));
        when(userDao.getUser(INVITEE_ID)).thenReturn(userWith(INVITEE_ID, User.Level.USER));
        when(inviteTokenDao.redeemToken(eq(TOKEN), any())).thenReturn(true);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.REDEEMED)));

        userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN));

        verify(userDao).updateUser(any());
    }

    @Test
    public void redeem_matchingUidAmongSeveral_succeeds() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID)).thenReturn(List.of(
                uid("dev.getelements.auth.scheme.google", "google-id-1", VerificationStatus.VERIFIED),
                emailUid("other@example.com", VerificationStatus.VERIFIED),
                emailUid(EMAIL, VerificationStatus.VERIFIED)));
        when(userDao.getUser(INVITEE_ID)).thenReturn(userWith(INVITEE_ID, User.Level.USER));
        when(inviteTokenDao.redeemToken(eq(TOKEN), any())).thenReturn(true);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.REDEEMED)));

        userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN));

        verify(userDao).updateUser(any());
    }

    @Test
    public void redeem_unverifiedEmail_forbiddenAndTokenUntouched() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID))
                .thenReturn(List.of(emailUid(EMAIL, VerificationStatus.UNVERIFIED)));

        assertThrows(ForbiddenException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));

        verify(userDao, never()).updateUser(any());
        verify(inviteTokenDao, never()).redeemToken(any(), any());
        verify(elementRegistry, never()).publish(any());
    }

    @Test
    public void redeem_pendingVerificationEmail_forbidden() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID))
                .thenReturn(List.of(emailUid(EMAIL, VerificationStatus.PENDING)));

        assertThrows(ForbiddenException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));

        verify(userDao, never()).updateUser(any());
        verify(inviteTokenDao, never()).redeemToken(any(), any());
    }

    @Test
    public void redeem_matchingIdButDifferentScheme_forbidden() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID)).thenReturn(List.of(
                uid("dev.getelements.auth.scheme.google", EMAIL, VerificationStatus.VERIFIED)));

        assertThrows(ForbiddenException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));

        verify(userDao, never()).updateUser(any());
        verify(inviteTokenDao, never()).redeemToken(any(), any());
    }

    @Test
    public void redeem_noEmailUidForUser_forbidden() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID)).thenReturn(List.of());

        assertThrows(ForbiddenException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));

        verify(userDao, never()).updateUser(any());
        verify(inviteTokenDao, never()).redeemToken(any(), any());
    }

    @Test
    public void redeem_unknownToken_notFound() {
        asUser();
        when(inviteTokenDao.findToken(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));

        verify(userDao, never()).updateUser(any());
        verify(inviteTokenDao, never()).redeemToken(any(), any());
    }

    @Test
    public void redeem_expiredToken_notFound() {
        asUser();
        // findToken filters expired tokens out at the DAO layer.
        when(inviteTokenDao.findToken(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));
    }

    @Test
    public void redeem_alreadyRedeemedToken_notFound() {
        asUser();
        when(inviteTokenDao.findToken(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));

        verify(userDao, never()).updateUser(any());
    }

    @Test
    public void redeem_revokedToken_notFound() {
        asUser();
        when(inviteTokenDao.findToken(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));
    }

    @Test
    public void redeem_nullToken_throwsInvalidParameter() {
        asUser();

        assertThrows(InvalidParameterException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(null)));

        verify(inviteTokenDao, never()).findToken(any());
    }

    @Test
    public void redeem_blankToken_throwsInvalidParameter() {
        asUser();

        assertThrows(InvalidParameterException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest("  ")));

        verify(inviteTokenDao, never()).findToken(any());
    }

    @Test
    public void redeem_unauthenticated_throwsUnauthorized() {
        // Guice cannot bind a null instance; the unauthenticated case is simulated by clearing the
        // request-scoped current user directly (the test lives in the impl's package).
        userInviteService.setCurrentUser(null);
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));

        assertThrows(UnauthorizedException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));

        verify(userDao, never()).updateUser(any());
        verify(inviteTokenDao, never()).redeemToken(any(), any());
    }

    @Test
    public void redeem_daoTransitionFails_notFoundAndNoElevation() {
        asUser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID))
                .thenReturn(List.of(emailUid(EMAIL, VerificationStatus.VERIFIED)));
        when(inviteTokenDao.redeemToken(eq(TOKEN), any())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userInviteService.redeemSuperUserInvite(redeemRequest(TOKEN)));

        verify(userDao, never()).updateUser(any());
        verify(elementRegistry, never()).publish(any());
    }

    @Test
    public void redeem_viaSuperUserService_alsoWorks() {
        asSuperuser();
        when(inviteTokenDao.findToken(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(userDao.getUser(INVITEE_ID)).thenReturn(userWith(INVITEE_ID, User.Level.SUPERUSER));
        when(inviteTokenDao.redeemToken(eq(TOKEN), any())).thenReturn(true);
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.REDEEMED)));

        superUserService.redeemSuperUserInvite(redeemRequest(TOKEN));

        verify(userDao).updateUser(any());
    }

    // =========================================================================
    // Revocation + listing (superuser-only)
    // =========================================================================

    @Test
    public void revoke_pendingInvite_marksRevokedAndPublishesEvent() {
        asSuperuser();
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.REVOKED)));
        when(inviteTokenDao.revokeToken(eq(TOKEN), any())).thenReturn(true);

        final var result = superUserService.revokeSuperUserInvite(TOKEN);

        assertNotNull(result);
        assertEquals(result.getStatus(), SuperUserInvite.Status.REVOKED);

        final var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(elementRegistry).publish(eventCaptor.capture());
        assertEquals(eventCaptor.getValue().getEventName(), AbstractSuperUserInviteService.INVITE_REVOKED_EVENT);
    }

    @Test
    public void revoke_unknownToken_notFound() {
        asSuperuser();
        when(inviteTokenDao.findTokenAnyState(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> superUserService.revokeSuperUserInvite(TOKEN));

        verify(inviteTokenDao, never()).revokeToken(any(), any());
    }

    @Test
    public void revoke_alreadyRedeemed_throwsInvalidParameter() {
        asSuperuser();
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.REDEEMED)));

        assertThrows(InvalidParameterException.class, () -> superUserService.revokeSuperUserInvite(TOKEN));

        verify(inviteTokenDao, never()).revokeToken(any(), any());
    }

    @Test
    public void revoke_daoTransitionFails_throwsInvalidParameter() {
        asSuperuser();
        when(inviteTokenDao.findTokenAnyState(TOKEN))
                .thenReturn(Optional.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(inviteTokenDao.revokeToken(eq(TOKEN), any())).thenReturn(false);

        assertThrows(InvalidParameterException.class, () -> superUserService.revokeSuperUserInvite(TOKEN));
    }

    @Test
    public void revoke_asUser_forbidden() {
        asUser();

        assertThrows(ForbiddenException.class, () -> userInviteService.revokeSuperUserInvite(TOKEN));

        verify(inviteTokenDao, never()).revokeToken(any(), any());
    }

    @Test
    public void list_returnsPaginationFromDao() {
        asSuperuser();
        final var pagination = new Pagination<SuperUserInvite>();
        pagination.setOffset(0);
        pagination.setTotal(1);
        pagination.setObjects(List.of(inviteFor(TOKEN, EMAIL, SuperUserInvite.Status.PENDING)));
        when(inviteTokenDao.getInvites(0, 20)).thenReturn(pagination);

        final var result = superUserService.listSuperUserInvites(0, 20);

        assertSame(result, pagination);
        assertEquals(result.getObjects().size(), 1);
    }

    @Test
    public void list_asUser_forbidden() {
        asUser();

        assertThrows(ForbiddenException.class, () -> userInviteService.listSuperUserInvites(0, 20));

        verify(inviteTokenDao, never()).getInvites(anyInt(), anyInt());
    }

    // =========================================================================
    // Phone matching (pre-existing behavior, must be preserved)
    // =========================================================================

    @Test
    public void phoneMatching_userLevel_stillWorks() {
        asUser();
        final var request = new InviteViaPhonesRequest();
        request.setPhoneNumbers(List.of("+1234567890"));

        when(userDao.getUsersByPrimaryPhoneNumbers(anyInt(), anyInt(), anyList()))
                .thenReturn(new Pagination<User>());

        final var response = userInviteService.inviteViaPhoneNumbers(request, 0, 20);

        assertNotNull(response);
        verify(userDao).getUsersByPrimaryPhoneNumbers(eq(0), eq(dev.getelements.elements.sdk.service.invite.InviteService.USERS_PER_PHONE_LIMIT), anyList());
    }

    @Test
    public void phoneMatching_unnormalizablePhonesDropped() {
        asUser();
        final var request = new InviteViaPhonesRequest();
        // Null and empty entries normalize to an empty Optional and are filtered before the DAO call.
        request.setPhoneNumbers(java.util.Arrays.asList("", null));

        when(userDao.getUsersByPrimaryPhoneNumbers(anyInt(), anyInt(), anyList()))
                .thenReturn(new Pagination<User>());

        userInviteService.inviteViaPhoneNumbers(request, 0, 20);

        final var listCaptor = ArgumentCaptor.forClass(List.class);
        verify(userDao).getUsersByPrimaryPhoneNumbers(anyInt(), anyInt(), listCaptor.capture());
        assertTrue(((List<?>) listCaptor.getValue()).isEmpty(), "Unnormalizable phone numbers must be filtered out.");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void asSuperuser() {
        currentUser.setId(INVITEE_ID);
        currentUser.setLevel(User.Level.SUPERUSER);
        when(userUidDao.getAllUserIdsForUser(INVITEE_ID))
                .thenReturn(List.of(emailUid(EMAIL, VerificationStatus.VERIFIED)));
    }

    private void asUser() {
        currentUser.setId(INVITEE_ID);
        currentUser.setLevel(User.Level.USER);
    }

    private static CreateSuperUserInviteRequest createRequest(final String email) {
        final var request = new CreateSuperUserInviteRequest();
        request.setEmail(email);
        return request;
    }

    private static RedeemSuperUserInviteRequest redeemRequest(final String token) {
        final var request = new RedeemSuperUserInviteRequest();
        request.setToken(token);
        return request;
    }

    private static User userWith(final String id, final User.Level level) {
        final var user = new User();
        user.setId(id);
        user.setLevel(level);
        return user;
    }

    private static SuperUserInvite inviteFor(final String token, final String email, final SuperUserInvite.Status status) {
        final var invite = new SuperUserInvite();
        invite.setToken(token);
        invite.setInvitedEmail(email);
        invite.setStatus(status);
        return invite;
    }

    private static UserUid emailUid(final String email, final VerificationStatus status) {
        return uid(SCHEME_EMAIL, email, status);
    }

    private static UserUid uid(final String scheme, final String id, final VerificationStatus status) {
        final var uid = new UserUid();
        uid.setScheme(scheme);
        uid.setId(id);
        uid.setUserId(INVITEE_ID);
        uid.setVerificationStatus(status);
        return uid;
    }

    private class TestModule extends AbstractModule {

        private final String baseUrl;

        TestModule(final String baseUrl) {
            this.baseUrl = baseUrl;
        }

        @Override
        protected void configure() {
            bind(SuperUserInviteTokenDao.class).toInstance(inviteTokenDao);
            bind(UserDao.class).toInstance(userDao);
            bind(UserUidDao.class).toInstance(userUidDao);
            bind(dev.getelements.elements.sdk.dao.ProfileDao.class).toInstance(profileDao);
            bind(EmailService.class).toInstance(emailService);
            bind(EmailTemplateService.class).annotatedWith(named(UNSCOPED)).toInstance(emailTemplateService);
            bind(ElementRegistry.class).toInstance(elementRegistry);
            bind(User.class).toInstance(currentUser);

            bindConstant().annotatedWith(named(SUPERUSER_INVITE_BASE_URL)).to(baseUrl);
            bindConstant().annotatedWith(named(SUPERUSER_INVITE_EXPIRY_HOURS)).to(EXPIRY_HOURS);
        }

    }

}

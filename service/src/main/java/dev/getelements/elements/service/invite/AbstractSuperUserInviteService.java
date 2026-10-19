package dev.getelements.elements.service.invite;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SuperUserInviteTokenDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import dev.getelements.elements.sdk.model.invite.CreateSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.sdk.model.invite.PhoneMatchedInvitation;
import dev.getelements.elements.sdk.model.invite.RedeemSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.service.email.EmailService;
import dev.getelements.elements.sdk.service.invite.InviteService;
import dev.getelements.elements.sdk.service.schema.email.EmailTemplateService;
import dev.getelements.elements.util.PhoneNormalizer;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static dev.getelements.elements.sdk.service.invite.InviteService.SUPERUSER_INVITE_BASE_URL;
import static dev.getelements.elements.sdk.service.invite.InviteService.SUPERUSER_INVITE_EXPIRY_HOURS;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Shared core for all {@link InviteService} implementations. Provides the superuser-invite
 * operations; subclasses apply access-level guards before delegating.
 */
abstract class AbstractSuperUserInviteService implements InviteService {

    static final String SUPERUSER_INVITE_EMAIL_TEMPLATE_KEY = "dev.getelements.elements.superuser.invite.email_template";

    static final String DEFAULT_SUBJECT = "Superuser Invitation";

    static final String DEFAULT_BODY =
            "<p>You have been invited to become a superuser.</p>"
          + "<p>Sign in to the admin console with the invited email address, then open the link below "
          + "to accept the invitation. The link is single-use and expires.</p>"
          + "<p><a href=\"{link}\">Accept Invitation</a></p>";

    static final String INVITE_CREATED_EVENT = "dev.getelements.superuser_invite.created";

    static final String INVITE_REDEEMED_EVENT = "dev.getelements.superuser_invite.redeemed";

    static final String INVITE_REVOKED_EVENT = "dev.getelements.superuser_invite.revoked";

    private static final long DEFAULT_TOKEN_VALIDITY_HOURS = 24;

    private UserDao userDao;

    private ProfileDao profileDao;

    private SuperUserInviteTokenDao inviteTokenDao;

    private UserUidDao userUidDao;

    private EmailService emailService;

    private EmailTemplateService emailTemplateService;

    private ElementRegistry elementRegistry;

    private User currentUser;

    private String inviteBaseUrl;

    private String expiryHours;

    // -------------------------------------------------------------------------
    // Phone-based invite matching (pre-existing behavior, shared by all levels)
    // -------------------------------------------------------------------------

    @Override
    public InviteViaPhonesResponse inviteViaPhoneNumbers(final InviteViaPhonesRequest inviteRequest, final int offset, final int count) {

        List<String> normalizedPhoneList = inviteRequest.getPhoneNumbers().stream()
                .map(PhoneNormalizer::normalizePhoneNb)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toUnmodifiableList());

        List<PhoneMatchedInvitation> invitations = new ArrayList<>();

        getUserDao().getUsersByPrimaryPhoneNumbers(0, USERS_PER_PHONE_LIMIT, normalizedPhoneList)
                .forEach(matchedUser -> invitations.add(createInvitation(matchedUser)));

        InviteViaPhonesResponse response = new InviteViaPhonesResponse();
        response.setMatched(invitations);
        return response;
    }

    private PhoneMatchedInvitation createInvitation(final User user) {
        PhoneMatchedInvitation invitation = new PhoneMatchedInvitation();
        invitation.setPhoneNumber(user.getPrimaryPhoneNb());

        List<String> profileIds = getProfileDao()
                .getActiveProfiles(0, PROFILES_PER_USER_LIMIT, null, user.getId(), null, null)
                .getObjects().stream().map(Profile::getId).collect(toUnmodifiableList());
        invitation.setProfileIds(profileIds);

        return invitation;
    }

    // -------------------------------------------------------------------------
    // Superuser invite operations
    // -------------------------------------------------------------------------

    /**
     * Core invite issuance. The caller must already hold superuser privileges; the current user is
     * recorded as the issuing administrator.
     *
     * @param request the invitation request
     * @return the created invitation
     */
    SuperUserInvite doCreateSuperUserInvite(final CreateSuperUserInviteRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidParameterException("A non-empty email address is required.");
        }

        final var email = request.getEmail().trim().toLowerCase();

        getInviteTokenDao().findPendingInviteByEmail(email).ifPresent(existing -> {
            throw new InvalidParameterException("An invitation is already pending for this email address.");
        });

        final var now = new Timestamp(System.currentTimeMillis());
        final var validityHours = request.getExpiryHours() != null
                ? request.getExpiryHours()
                : parseExpiryHours();
        final var expiry = new Timestamp(now.getTime() + TimeUnit.HOURS.toMillis(validityHours));

        final var token = getInviteTokenDao().createInvite(getCurrentUser(), email, expiry, now);

        sendInviteEmail(token, email);

        getElementRegistry().publish(Event.builder()
                .named(INVITE_CREATED_EVENT)
                .argument(token)
                .build());

        return getInviteTokenDao().findTokenAnyState(token)
                .orElseThrow(() -> new IllegalStateException("Invite vanished immediately after creation: " + token));

    }

    /**
     * Core invite listing.
     *
     * @param offset the pagination offset
     * @param count  the page size
     * @return the paginated invitations
     */
    Pagination<SuperUserInvite> doListSuperUserInvites(final int offset, final int count) {
        return getInviteTokenDao().getInvites(offset, count);
    }

    /**
     * Core invite revocation. Only pending invitations can be revoked.
     *
     * @param token the invitation token
     * @return the revoked invitation
     */
    SuperUserInvite doRevokeSuperUserInvite(final String token) {

        final var existing = getInviteTokenDao().findTokenAnyState(token)
                .orElseThrow(() -> new NotFoundException("No such invitation."));

        if (!SuperUserInvite.Status.PENDING.equals(existing.getStatus())) {
            throw new InvalidParameterException("Only pending invitations can be revoked.");
        }

        final var now = new Timestamp(System.currentTimeMillis());

        if (!getInviteTokenDao().revokeToken(token, now)) {
            throw new InvalidParameterException("Only pending invitations can be revoked.");
        }

        getElementRegistry().publish(Event.builder()
                .named(INVITE_REVOKED_EVENT)
                .argument(token)
                .build());

        return getInviteTokenDao().findTokenAnyState(token)
                .orElseThrow(() -> new IllegalStateException("Invite vanished during revocation: " + token));

    }

    /**
     * Core invite redemption. Requires an authenticated caller whose account holds a verified email
     * UID matching the invited address. Elevates the caller to {@link User.Level#SUPERUSER}.
     *
     * @param request the redemption request
     * @return the redeemed invitation
     */
    SuperUserInvite doRedeemSuperUserInvite(final RedeemSuperUserInviteRequest request) {

        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new InvalidParameterException("A non-empty token is required.");
        }

        final var user = getCurrentUser();

        if (user == null || user.getId() == null) {
            throw new UnauthorizedException("Sign in with the invited account to redeem this invitation.");
        }

        final var invite = getInviteTokenDao().findToken(request.getToken())
                .orElseThrow(() -> new NotFoundException("Invalid or expired invitation."));

        if (!controlsInvitedEmail(user, invite.getInvitedEmail())) {
            throw new ForbiddenException("This invitation was issued for a different email address.");
        }

        final var now = new Timestamp(System.currentTimeMillis());

        if (!getInviteTokenDao().redeemToken(request.getToken(), now)) {
            throw new NotFoundException("Invalid or expired invitation.");
        }

        final var freshUser = getUserDao().getUser(user.getId());
        freshUser.setLevel(User.Level.SUPERUSER);
        getUserDao().updateUser(freshUser);

        getElementRegistry().publish(Event.builder()
                .named(INVITE_REDEEMED_EVENT)
                .argument(request.getToken())
                .build());

        return getInviteTokenDao().findTokenAnyState(request.getToken())
                .orElseThrow(() -> new IllegalStateException("Invite vanished during redemption: " + request.getToken()));

    }

    private boolean controlsInvitedEmail(final User user, final String invitedEmail) {

        if (invitedEmail == null || invitedEmail.isBlank()) {
            return false;
        }

        final var target = invitedEmail.trim().toLowerCase();

        // The account must hold an email-scheme UID matching the invited address. A verified UID is
        // required so that an unverified (merely asserted) address cannot claim an admin-elevation
        // invite. OIDC-linked addresses are marked VERIFIED by their provider; password accounts
        // must have completed the email-verification flow.
        return getUserUidDao().getAllUserIdsForUser(user.getId()).stream()
                .anyMatch(uid -> SCHEME_EMAIL.equals(uid.getScheme())
                        && target.equals(uid.getId() == null ? null : uid.getId().trim().toLowerCase())
                        && VerificationStatus.VERIFIED.equals(uid.getVerificationStatus()));

    }

    private void sendInviteEmail(final String token, final String email) {

        final var baseUrl = getInviteBaseUrl() == null || getInviteBaseUrl().isBlank()
                ? null
                : getInviteBaseUrl().replaceAll("/$", "");

        if (baseUrl == null) {
            // Without a configured base URL the email cannot carry a redeemable link. The invite
            // record still exists (and remains revocable); the token can be surfaced out-of-band.
            return;
        }

        final var template = getEmailTemplateService().getOrCreateEmailTemplate(
                SUPERUSER_INVITE_EMAIL_TEMPLATE_KEY,
                "Superuser Invitation",
                DEFAULT_SUBJECT,
                DEFAULT_BODY);

        final var link = baseUrl + "?token=" + token;
        final var body = template.getBody().replace("{link}", link);

        getEmailService().send(null, email, template.getSubject(), body, true);

    }

    private long parseExpiryHours() {
        try {
            return Long.parseLong(expiryHours.trim());
        } catch (final NumberFormatException e) {
            return DEFAULT_TOKEN_VALIDITY_HOURS;
        }
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public SuperUserInviteTokenDao getInviteTokenDao() {
        return inviteTokenDao;
    }

    @Inject
    public void setInviteTokenDao(SuperUserInviteTokenDao inviteTokenDao) {
        this.inviteTokenDao = inviteTokenDao;
    }

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    @Inject
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public EmailTemplateService getEmailTemplateService() {
        return emailTemplateService;
    }

    @Inject
    public void setEmailTemplateService(@Named(UNSCOPED) EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    @Inject
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public String getInviteBaseUrl() {
        return inviteBaseUrl;
    }

    @Inject
    public void setInviteBaseUrl(@Named(SUPERUSER_INVITE_BASE_URL) String inviteBaseUrl) {
        this.inviteBaseUrl = inviteBaseUrl;
    }

    public String getExpiryHours() {
        return expiryHours;
    }

    @Inject
    public void setExpiryHours(@Named(SUPERUSER_INVITE_EXPIRY_HOURS) String expiryHours) {
        this.expiryHours = expiryHours;
    }

}

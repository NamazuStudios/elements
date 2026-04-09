package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.UidVerificationTokenDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import dev.getelements.elements.sdk.service.email.EmailService;
import dev.getelements.elements.sdk.service.user.EmailVerificationService;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import static dev.getelements.elements.sdk.service.user.EmailVerificationService.*;

/**
 * Shared base for all {@link EmailVerificationService} implementations.
 *
 * <p>Provides the core {@link #doRequestVerification} and {@link #doCompleteVerification} operations,
 * plus the configurable email body template. Subclasses apply access-level-specific guards before
 * delegating to these methods.
 */
abstract class AbstractEmailVerificationService implements EmailVerificationService {

    static final long TOKEN_VALIDITY_MS = TimeUnit.HOURS.toMillis(24);

    private UserUidDao userUidDao;

    private UidVerificationTokenDao tokenDao;

    private EmailService emailService;

    private ElementRegistry elementRegistry;

    private String emailSubject;

    private String emailTemplate;

    // -------------------------------------------------------------------------
    // Shared core operations
    // -------------------------------------------------------------------------

    /**
     * Creates a token, sends the verification email, and moves the UID to PENDING.
     *
     * @param ownerUser           the user that owns the email UID
     * @param email               the email address (UID id) to verify
     * @param verificationBaseUrl base URL; the service appends {@code ?token=<token>}
     * @return the updated UID with status PENDING
     */
    UserUid doRequestVerification(final User ownerUser,
                                  final String email,
                                  final String verificationBaseUrl) {

        final var expiry = new Timestamp(System.currentTimeMillis() + TOKEN_VALIDITY_MS);
        final var token = getTokenDao().createToken(ownerUser, SCHEME_EMAIL, email, expiry);

        final var link = verificationBaseUrl + "?token=" + token;
        final var body = getEmailTemplate().replace("{link}", link);

        getEmailService().send(null, email, getEmailSubject(), body, true);

        final var updated = getUserUidDao().updateVerificationStatus(email, SCHEME_EMAIL, VerificationStatus.PENDING);

        getElementRegistry().publish(Event.builder()
                .argument(updated)
                .named(EMAIL_VERIFICATION_REQUESTED_EVENT)
                .build());

        return updated;
    }

    /**
     * Consumes a single-use token and moves the UID to VERIFIED.
     *
     * @param token the opaque verification token
     * @return the updated UID with status VERIFIED
     */
    UserUid doCompleteVerification(final String token) {

        final var tokenData = getTokenDao().findToken(token)
                .orElseThrow(() -> new NotFoundException("Verification token not found or has expired."));

        getTokenDao().deleteToken(token);

        final var updated = getUserUidDao().updateVerificationStatus(
                tokenData.getUidId(), tokenData.getScheme(), VerificationStatus.VERIFIED);

        getElementRegistry().publish(Event.builder()
                .argument(updated)
                .named(EMAIL_VERIFICATION_COMPLETED_EVENT)
                .build());

        return updated;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
    }

    public UidVerificationTokenDao getTokenDao() {
        return tokenDao;
    }

    @Inject
    public void setTokenDao(UidVerificationTokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    @Inject
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    @Inject
    public void setEmailSubject(@Named(VERIFICATION_EMAIL_SUBJECT) String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailTemplate() {
        return emailTemplate;
    }

    @Inject
    public void setEmailTemplate(@Named(VERIFICATION_EMAIL_TEMPLATE) String emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

}

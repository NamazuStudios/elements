package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.PasswordResetTokenDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.service.email.EmailService;
import dev.getelements.elements.sdk.service.user.PasswordResetService;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import static dev.getelements.elements.sdk.service.user.PasswordResetService.*;

/**
 * Shared base for all {@link PasswordResetService} implementations.
 *
 * <p>Provides the core {@link #doRequestReset} and {@link #doCompleteReset} operations.
 * Subclasses apply access-level-specific guards before delegating to these methods.
 */
abstract class AbstractPasswordResetService implements PasswordResetService {

    private static final long DEFAULT_TOKEN_VALIDITY_HOURS = 1;

    /** Per-email cooldown: suppress duplicate reset emails sent within 60 seconds. */
    private static final ConcurrentHashMap<String, Long> LAST_REQUEST_BY_EMAIL = new ConcurrentHashMap<>();

    private static final long EMAIL_COOLDOWN_MS = TimeUnit.SECONDS.toMillis(60);

    private UserUidDao userUidDao;

    private PasswordResetTokenDao tokenDao;

    private UserDao userDao;

    private EmailService emailService;

    private ElementRegistry elementRegistry;

    private String emailSubject;

    private String emailTemplate;

    private String expiryHours;

    // -------------------------------------------------------------------------
    // Shared core operations
    // -------------------------------------------------------------------------

    /**
     * Looks up the user by email, creates a reset token, and sends the reset email.
     * Always returns normally even if the email is not registered (no user enumeration).
     *
     * @param email        the email address to send the reset link to
     * @param resetBaseUrl base URL; the service appends {@code ?token=<token>}
     */
    void doRequestReset(final String email, final String resetBaseUrl) {

        final var normalised = email.trim().toLowerCase();

        final var uid = getUserUidDao().findUserUid(normalised, SCHEME_EMAIL);

        if (uid.isEmpty()) {
            return; // silently ignore — no user enumeration
        }

        final var now = System.currentTimeMillis();
        final var lastSent = LAST_REQUEST_BY_EMAIL.get(normalised);

        if (lastSent != null && (now - lastSent) < EMAIL_COOLDOWN_MS) {
            return; // within cooldown window — suppress duplicate
        }

        LAST_REQUEST_BY_EMAIL.put(normalised, now);

        final var user = getUserDao().getUser(uid.get().getUserId());
        final var expiry = new Timestamp(now + getTokenValidityMs());
        final var token = getTokenDao().createToken(user, expiry);

        final var link = resetBaseUrl + "?token=" + token;
        final var body = getEmailTemplate().replace("{link}", link);

        getEmailService().send(null, normalised, getEmailSubject(), body, true);

        getElementRegistry().publish(Event.builder()
                .named(PASSWORD_RESET_REQUESTED_EVENT)
                .build());
    }

    /**
     * Validates the token, sets the new password, and deletes the token.
     *
     * @param token       the opaque reset token
     * @param newPassword the new password to set
     */
    void doCompleteReset(final String token, final String newPassword) {
        final var tokenData = getTokenDao().findToken(token)
                .orElseThrow(() -> new InvalidParameterException("Invalid or expired reset token."));

        getUserDao().setPassword(tokenData.getUser().getId(), newPassword);
        getTokenDao().deleteToken(token);

        getElementRegistry().publish(Event.builder()
                .named(PASSWORD_RESET_COMPLETED_EVENT)
                .build());
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

    public PasswordResetTokenDao getTokenDao() {
        return tokenDao;
    }

    @Inject
    public void setTokenDao(PasswordResetTokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
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
    public void setEmailSubject(@Named(RESET_EMAIL_SUBJECT) String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailTemplate() {
        return emailTemplate;
    }

    @Inject
    public void setEmailTemplate(@Named(RESET_EMAIL_TEMPLATE) String emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public String getExpiryHours() {
        return expiryHours;
    }

    @Inject
    public void setExpiryHours(@Named(RESET_TOKEN_EXPIRY_HOURS) String expiryHours) {
        this.expiryHours = expiryHours;
    }

    private long getTokenValidityMs() {
        try {
            return TimeUnit.HOURS.toMillis(Long.parseLong(expiryHours.trim()));
        } catch (final NumberFormatException e) {
            return TimeUnit.HOURS.toMillis(DEFAULT_TOKEN_VALIDITY_HOURS);
        }
    }

}

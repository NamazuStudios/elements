package dev.getelements.elements.sdk.service.user;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Service for initiating and completing a self-service password reset via email.
 *
 * <p>The reset lifecycle is:
 * <ol>
 *   <li>Caller invokes {@link #requestReset} — a time-limited token is created and a reset email is sent.</li>
 *   <li>User clicks the link — {@link #completeReset} validates the token, sets the new password, and
 *       invalidates all existing sessions.</li>
 * </ol>
 *
 * <p>Both methods always return normally regardless of whether the email is registered, to prevent
 * user enumeration attacks.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
@ElementEventProducer(
        value = PasswordResetService.PASSWORD_RESET_REQUESTED_EVENT,
        description = "Fired when a password reset is requested.")
@ElementEventProducer(
        value = PasswordResetService.PASSWORD_RESET_COMPLETED_EVENT,
        description = "Fired when a password reset is successfully completed.")
public interface PasswordResetService {

    /** Event fired when a password reset request is initiated. */
    String PASSWORD_RESET_REQUESTED_EVENT = "dev.getelements.password_reset.requested";

    /** Event fired when a password reset is successfully completed. */
    String PASSWORD_RESET_COMPLETED_EVENT = "dev.getelements.password_reset.completed";

    /**
     * Optional system/element attribute: fully-qualified public base URL for reset links.
     * If not set the REST layer will derive the URL from the incoming request.
     */
    @ElementDefaultAttribute(
            value = "",
            description = "Public base URL for password reset links. "
                        + "If blank, the REST layer derives the URL from the incoming request.")
    String RESET_BASE_URL = "dev.getelements.elements.password_reset.base_url";

    /**
     * Element attribute: subject line for the password reset email.
     */
    @ElementDefaultAttribute(
            value = "Reset your password",
            description = "Subject line for password reset emails.")
    String RESET_EMAIL_SUBJECT = "dev.getelements.elements.password_reset.email_subject";

    /**
     * Element attribute: HTML body template for the password reset email.
     * Must contain the literal token {@code {link}}, which is replaced with the full reset URL.
     */
    @ElementDefaultAttribute(
            value = "<p>Click the link below to reset your password. "
                  + "This link expires in 1 hour.</p>"
                  + "<p><a href=\"{link}\">Reset Password</a></p>",
            description = "HTML email body template for password reset emails. "
                        + "Use {link} as a placeholder for the reset URL.")
    String RESET_EMAIL_TEMPLATE = "dev.getelements.elements.password_reset.email_template";

    /**
     * Element attribute: token validity in hours.
     */
    @ElementDefaultAttribute(
            value = "1",
            description = "Token validity in hours.")
    String RESET_TOKEN_EXPIRY_HOURS = "dev.getelements.elements.password_reset.expiry_hours";

    /**
     * Initiates a password reset for the given email address.
     * Sends a reset email if the email is registered; always returns normally (no user enumeration).
     *
     * @param email        the email address of the account to reset
     * @param resetBaseUrl the caller-supplied URL prefix; the service appends {@code ?token=<token>}
     */
    void requestReset(String email, String resetBaseUrl);

    /**
     * Completes the password reset by consuming a single-use token and setting the new password.
     * All existing sessions are automatically invalidated because the password hash changes.
     *
     * @param token       the opaque reset token from the email link
     * @param newPassword the new password to set
     */
    void completeReset(String token, String newPassword);

}

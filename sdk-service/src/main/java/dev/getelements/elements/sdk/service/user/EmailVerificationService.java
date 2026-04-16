package dev.getelements.elements.sdk.service.user;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.user.UserUid;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Service for initiating and completing email-based verification of a {@link UserUid}.
 *
 * <p>The verification lifecycle is:
 * <ol>
 *   <li>Caller invokes {@link #requestVerification} — status moves to PENDING and an email is sent.</li>
 *   <li>User clicks the link — {@link #completeVerification} is invoked — status moves to VERIFIED.</li>
 * </ol>
 *
 * <b>Email template customisation</b>
 * <p>The body of the verification email is controlled by the {@link #VERIFICATION_EMAIL_TEMPLATE} element
 * attribute. Set this attribute in your Element to override the default template. The template must contain
 * the literal token {@code {link}}, which is replaced at send-time with the full verification URL
 * (e.g. {@code https://example.com/api/rest/verify?token=abc123}). The email is always sent as
 * {@code text/html}. Example custom template:
 *
 * <pre>
 * &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;h1&gt;Verify your account&lt;/h1&gt;
 *     &lt;p&gt;&lt;a href="{link}"&gt;Click here to verify&lt;/a&gt;&lt;/p&gt;
 *   &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
@ElementEventProducer(
        value = EmailVerificationService.EMAIL_VERIFICATION_REQUESTED_EVENT,
        parameters = UserUid.class,
        description = "Fired when an email verification is requested; the UID status moves to PENDING.")
@ElementEventProducer(
        value = EmailVerificationService.EMAIL_VERIFICATION_COMPLETED_EVENT,
        parameters = UserUid.class,
        description = "Fired when an email verification is completed; the UID status moves to VERIFIED.")
public interface EmailVerificationService {

    /** Event fired when a verification request is initiated. */
    String EMAIL_VERIFICATION_REQUESTED_EVENT = "dev.getelements.email_verification.requested";

    /** Event fired when verification is successfully completed. */
    String EMAIL_VERIFICATION_COMPLETED_EVENT = "dev.getelements.email_verification.completed";

    /**
     * Optional system/element attribute: fully-qualified public base URL for verification links.
     * If not set the REST layer will derive the URL from the incoming request.
     */
    @ElementDefaultAttribute(
            value = "",
            description = "Public base URL for verification links (e.g. https://example.com/api/rest/verify). "
                        + "If blank, the REST layer derives the URL from the incoming request.")
    String VERIFICATION_BASE_URL = "dev.getelements.elements.verification.base_url";

    /**
     * Element attribute: subject line for the verification email.
     * Defaults to {@code "Verify your email"}.
     */
    @ElementDefaultAttribute(
            value = "Verify your email",
            description = "Subject line for verification emails.")
    String VERIFICATION_EMAIL_SUBJECT = "dev.getelements.elements.verification.email_subject";

    /**
     * Element attribute: HTML body template for the verification email.
     * Must contain the literal token {@code {link}}, which is replaced with the full verification URL.
     *
     * <p>Defaults to a plain inline link. Override this in your Element to provide a branded template.
     */
    @ElementDefaultAttribute(
            value = "<p>Please verify your email address by clicking the link below:</p>"
                  + "<p><a href=\"{link}\">Verify Email</a></p>",
            description = "HTML email body template for verification emails. "
                        + "Use {link} as a placeholder for the verification URL.")
    String VERIFICATION_EMAIL_TEMPLATE = "dev.getelements.elements.verification.email_template";

    /**
     * Initiates email verification for the given email address of the current user.
     * Moves the UID status from UNVERIFIED to PENDING and sends the verification email.
     *
     * @param email               the email address to verify
     * @param verificationBaseUrl the caller-supplied URL prefix; the service appends {@code ?token=<token>}
     * @return the updated {@link UserUid} with status PENDING
     */
    UserUid requestVerification(String email, String verificationBaseUrl);

    /**
     * Completes verification by consuming a single-use token from the email link.
     * Moves the UID status from PENDING to VERIFIED.
     *
     * @param token the opaque verification token
     * @return the updated {@link UserUid} with status VERIFIED
     */
    UserUid completeVerification(String token);

}

package dev.getelements.elements.sdk.model.exception.auth;

import dev.getelements.elements.sdk.model.exception.ErrorCode;

/**
 * Thrown when an external identity (e.g. an OIDC {@code sub} or OAuth2 user id) presented for linking or
 * login is already linked to a different user than the one it is being linked to.
 *
 * <p>Distinct from the generic {@link AuthValidationException FORBIDDEN} failures so clients can detect the
 * collision programmatically (e.g. to prompt the user to unlink the identity from the other account, or
 * offer account-merge UX) instead of matching on the exception message text. Maps to HTTP 409 CONFLICT.</p>
 *
 * <p>Extends {@link AuthValidationException} so existing handlers for that type continue to work.</p>
 */
public class IdentityAlreadyLinkedException extends AuthValidationException {

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public IdentityAlreadyLinkedException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.IDENTITY_ALREADY_LINKED;
    }

}

package dev.getelements.elements.sdk.model.exception.auth;

import dev.getelements.elements.sdk.model.exception.BaseException;
import dev.getelements.elements.sdk.model.exception.ErrorCode;

/**
 * Thrown by the OIDC provider's authorization and token endpoints to signal an RFC 6749 OAuth 2.0 error. The
 * exception carries the protocol-level {@code error} code (e.g. {@code invalid_grant}, {@code invalid_client})
 * and a human-readable description; the REST layer's exception mapper renders it as an
 * {@link dev.getelements.elements.sdk.model.oidc.OAuth2ErrorResponse} with the appropriate HTTP status.
 *
 * <p>For errors raised by the authorization endpoint, {@code redirectUri} and {@code state} may be populated
 * once the client and redirect URI have been validated; the resource then redirects the browser back to the
 * relying party with the error parameters, per RFC 6749 section 4.1.2.1, rather than rendering an error
 * document. When they are null the error is not redirect-able (e.g. an unknown client or an unregistered
 * redirect URI).</p>
 */
public class OAuth2Exception extends BaseException {

    private final String error;

    private final String errorDescription;

    private String redirectUri;

    private String state;

    /**
     * Creates a new instance.
     *
     * @param error the OAuth 2.0 error code
     * @param errorDescription a human-readable description
     */
    public OAuth2Exception(final String error, final String errorDescription) {
        super(errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
    }

    /**
     * Creates a new instance with a cause.
     *
     * @param error the OAuth 2.0 error code
     * @param errorDescription a human-readable description
     * @param cause the cause
     */
    public OAuth2Exception(final String error, final String errorDescription, final Throwable cause) {
        super(errorDescription, cause);
        this.error = error;
        this.errorDescription = errorDescription;
    }

    /** @return the OAuth 2.0 error code */
    public String getError() {
        return error;
    }

    /** @return a human-readable description of the error */
    public String getErrorDescription() {
        return errorDescription;
    }

    /** @return the validated redirect URI, when the error may be redirected to the client */
    public String getRedirectUri() {
        return redirectUri;
    }

    /**
     * Sets the redirect URI the authorization endpoint should send the browser to with the error parameters.
     *
     * @param redirectUri the validated redirect URI
     */
    public void setRedirectUri(final String redirectUri) {
        this.redirectUri = redirectUri;
    }

    /** @return the client's opaque state value, echoed back on redirect */
    public String getState() {
        return state;
    }

    /**
     * Sets the client's opaque state value to echo back on redirect.
     *
     * @param state the client state
     */
    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.INVALID_DATA;
    }

}

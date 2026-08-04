package dev.getelements.elements.sdk.model.session;

/**
 * The result of handling an OIDC provider's redirect callback. Service-layer only — not part of the public
 * REST API. Carries everything {@code OidcCallbackResource} needs to decide how to respond, so all callback
 * outcome handling (including recognizing failure) lives in the service layer rather than the REST resource.
 */
public class OidcLoginAttemptCallbackResult {

    /** Creates a new instance. */
    public OidcLoginAttemptCallbackResult() {}

    private boolean success;

    private String redirectUrl;

    /**
     * Builds a successful result.
     *
     * @param redirectUrl the caller-configured success redirect URL, or {@code null} if none was set
     * @return the result
     */
    public static OidcLoginAttemptCallbackResult success(final String redirectUrl) {
        final var result = new OidcLoginAttemptCallbackResult();
        result.setSuccess(true);
        result.setRedirectUrl(redirectUrl);
        return result;
    }

    /**
     * Builds a failed result.
     *
     * @param redirectUrl the caller-configured error redirect URL, or {@code null} if none was set
     * @return the result
     */
    public static OidcLoginAttemptCallbackResult failure(final String redirectUrl) {
        final var result = new OidcLoginAttemptCallbackResult();
        result.setSuccess(false);
        result.setRedirectUrl(redirectUrl);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

}

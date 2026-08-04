package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Request body for {@code POST /oidc/session}. Supplying only {@link #getProvider()} starts a pending,
 * browser-redirect login attempt. Additionally supplying {@link #getIdToken()} instead performs direct id_token
 * validation and returns a completed session synchronously, sharing validation code with the callback path.
 */
@Schema(description = "Starts a browser-redirect OIDC login attempt, or, if idToken is supplied, " +
        "validates a possessed id_token directly and returns a completed session synchronously.")
public class OidcLoginAttemptRequest {

    /** Creates a new instance. */
    public OidcLoginAttemptRequest() {}

    @NotBlank
    @Schema(description = "The provider identifier (e.g. 'twitch').")
    private String provider;

    @Schema(description = "An already-possessed id_token to validate directly, skipping the browser-redirect flow.")
    private String idToken;

    @Schema(description = "Optional. If set, GET /oidc/{provider}/callback redirects the browser here on a " +
            "successful login instead of rendering the default success page.")
    private String successRedirectUrl;

    @Schema(description = "Optional. If set, GET /oidc/{provider}/callback redirects the browser here on a " +
            "failed login instead of rendering the default error page.")
    private String errorRedirectUrl;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getSuccessRedirectUrl() {
        return successRedirectUrl;
    }

    public void setSuccessRedirectUrl(String successRedirectUrl) {
        this.successRedirectUrl = successRedirectUrl;
    }

    public String getErrorRedirectUrl() {
        return errorRedirectUrl;
    }

    public void setErrorRedirectUrl(String errorRedirectUrl) {
        this.errorRedirectUrl = errorRedirectUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OidcLoginAttemptRequest that)) return false;
        return Objects.equals(provider, that.provider)
                && Objects.equals(idToken, that.idToken)
                && Objects.equals(successRedirectUrl, that.successRedirectUrl)
                && Objects.equals(errorRedirectUrl, that.errorRedirectUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, idToken, successRedirectUrl, errorRedirectUrl);
    }

    @Override
    public String toString() {
        return "OidcLoginAttemptRequest{" +
                "provider='" + provider + '\'' +
                '}';
    }

}

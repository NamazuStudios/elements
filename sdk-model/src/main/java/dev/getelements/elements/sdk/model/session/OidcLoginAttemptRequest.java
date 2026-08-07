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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OidcLoginAttemptRequest that)) return false;
        return Objects.equals(provider, that.provider)
                && Objects.equals(idToken, that.idToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, idToken);
    }

    @Override
    public String toString() {
        return "OidcLoginAttemptRequest{" +
                "provider='" + provider + '\'' +
                '}';
    }

}

package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Request body for {@code POST /oidc/session/{id}/confirm}, which finalizes an account-linking browser-redirect
 * OIDC login attempt. {@link #getConfirmToken()} is the secret returned only in the original
 * {@code POST /oidc/session} response for that attempt.
 */
@Schema(description = "Finalizes an account-linking OIDC login attempt by presenting the confirmToken returned " +
        "when the attempt was started.")
public class OidcLoginAttemptConfirmRequest {

    /** Creates a new instance. */
    public OidcLoginAttemptConfirmRequest() {}

    @NotBlank
    @Schema(description = "The confirmToken returned in the original POST /oidc/session response.")
    private String confirmToken;

    public String getConfirmToken() {
        return confirmToken;
    }

    public void setConfirmToken(String confirmToken) {
        this.confirmToken = confirmToken;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OidcLoginAttemptConfirmRequest that)) return false;
        return Objects.equals(confirmToken, that.confirmToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(confirmToken);
    }

    @Override
    public String toString() {
        return "OidcLoginAttemptConfirmRequest{confirmToken='[redacted]'}";
    }

}

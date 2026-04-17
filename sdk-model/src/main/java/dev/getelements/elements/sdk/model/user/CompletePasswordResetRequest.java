package dev.getelements.elements.sdk.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for completing a password reset using a single-use token.
 */
@Schema(description = "Request to complete a password reset using the token from the reset email.")
public class CompletePasswordResetRequest {

    @NotBlank
    @Schema(description = "The opaque reset token from the password reset email.")
    private String token;

    @NotBlank
    @Schema(description = "The new password.")
    private String password;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

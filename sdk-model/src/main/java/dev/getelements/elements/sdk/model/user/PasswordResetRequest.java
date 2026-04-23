package dev.getelements.elements.sdk.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for initiating a password reset.
 */
@Schema(description = "Request to initiate a password reset for an account.")
public class PasswordResetRequest {

    @NotNull
    @Email
    @Schema(description = "The email address associated with the account.")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

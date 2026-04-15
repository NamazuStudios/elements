package dev.getelements.elements.sdk.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Request body for initiating email verification of a {@link UserUid}. */
@Schema
public class EmailVerificationRequest {

    @NotNull
    @Schema(description = "The email address to verify.")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

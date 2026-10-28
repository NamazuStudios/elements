package dev.getelements.elements.sdk.model.invite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a superuser invitation.
 */
@Schema(description = "Request to invite a user by email to become a superuser.")
public class CreateSuperUserInviteRequest {

    @NotNull
    @Email
    @Schema(description = "The email address to invite.")
    private String email;

    @Schema(description = "Token validity in hours. If null, defaults to the service configuration.")
    private Integer expiryHours;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getExpiryHours() {
        return expiryHours;
    }

    public void setExpiryHours(Integer expiryHours) {
        this.expiryHours = expiryHours;
    }
}

package dev.getelements.elements.sdk.model.invite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for redeeming a superuser invitation.
 */
@Schema(description = "Request to redeem a superuser invitation token.")
public class RedeemSuperUserInviteRequest {

    @NotBlank
    @Schema(description = "The opaque invitation token.")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

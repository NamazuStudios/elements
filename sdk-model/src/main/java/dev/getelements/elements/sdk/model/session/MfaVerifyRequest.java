package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Represents the second step of a username/password login that required a TOTP challenge: the opaque
 * challenge ID returned by the first step, plus either a current TOTP code or a one-time recovery code.
 */
@Schema(description = "Completes a pending MFA challenge from a username/password login.")
public class MfaVerifyRequest {

    /** Creates a new instance. */
    public MfaVerifyRequest() {}

    @NotBlank
    @Schema(description = "The challenge ID returned when the login first required a second factor.")
    private String challengeId;

    @NotBlank
    @Schema(description = "The current TOTP code from the authenticator app, or a one-time recovery code.")
    private String code;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MfaVerifyRequest that = (MfaVerifyRequest) o;
        return Objects.equals(challengeId, that.challengeId) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(challengeId, code);
    }

    @Override
    public String toString() {
        return "MfaVerifyRequest{challengeId='" + challengeId + "', code='...'}";
    }

}

package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/** An error response returned when a login requires a second authentication factor to complete. */
@Schema(description = "Returned when a login requires a second authentication factor (TOTP) to complete.")
public class MfaChallengeErrorResponse extends ErrorResponse {

    /** Creates a new instance. */
    public MfaChallengeErrorResponse() {}

    @Schema(description = "The opaque challenge ID to submit, with a code, to POST session/mfa.")
    private String challengeId;

    @Schema(description = "The epoch millisecond timestamp at which the challenge expires.")
    private long expiresAt;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MfaChallengeErrorResponse that = (MfaChallengeErrorResponse) o;
        return expiresAt == that.expiresAt && Objects.equals(challengeId, that.challengeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), challengeId, expiresAt);
    }

    @Override
    public String toString() {
        return "MfaChallengeErrorResponse{challengeId='" + challengeId + "', expiresAt=" + expiresAt + '}';
    }

}

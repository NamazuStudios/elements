package dev.getelements.elements.sdk.model.exception.auth;

import dev.getelements.elements.sdk.model.exception.BaseException;
import dev.getelements.elements.sdk.model.exception.ErrorCode;

/**
 * Thrown by a username/password login when the account has TOTP two-factor authentication enrolled and
 * enforced. Carries the opaque challenge ID the caller must submit, along with a code, to
 * {@code POST session/mfa} to complete the login.
 */
public class MfaChallengeRequiredException extends BaseException {

    private final String challengeId;

    private final long expiresAt;

    /**
     * Creates a new instance.
     *
     * @param challengeId the opaque challenge ID
     * @param expiresAt the epoch millisecond timestamp at which the challenge expires
     */
    public MfaChallengeRequiredException(final String challengeId, final long expiresAt) {
        super("A second authentication factor is required to complete this login.");
        this.challengeId = challengeId;
        this.expiresAt = expiresAt;
    }

    /**
     * Returns the opaque challenge ID.
     *
     * @return the challenge ID
     */
    public String getChallengeId() {
        return challengeId;
    }

    /**
     * Returns the epoch millisecond timestamp at which the challenge expires.
     *
     * @return the expiry timestamp
     */
    public long getExpiresAt() {
        return expiresAt;
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.MFA_REQUIRED;
    }

}

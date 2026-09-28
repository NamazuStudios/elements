package dev.getelements.elements.sdk.model.auth;

import java.util.List;
import java.util.Objects;

/**
 * Internal, DAO-level model of a single user's TOTP enrollment state. Not part of the public REST API --
 * carries the raw shared secret and hashed recovery codes, so it must never be mapped onto the public
 * {@link dev.getelements.elements.sdk.model.user.User} model, mirroring how the password hash/salt are kept
 * off of it today.
 */
public class TotpUserState {

    /** Creates a new instance. */
    public TotpUserState() {}

    private String userId;

    private String secret;

    private boolean enabled;

    private List<String> recoveryCodeHashes;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getRecoveryCodeHashes() {
        return recoveryCodeHashes;
    }

    public void setRecoveryCodeHashes(List<String> recoveryCodeHashes) {
        this.recoveryCodeHashes = recoveryCodeHashes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TotpUserState that = (TotpUserState) o;
        return enabled == that.enabled
                && Objects.equals(userId, that.userId)
                && Objects.equals(secret, that.secret)
                && Objects.equals(recoveryCodeHashes, that.recoveryCodeHashes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, secret, enabled, recoveryCodeHashes);
    }

    @Override
    public String toString() {
        return "TotpUserState{userId='" + userId + "', enabled=" + enabled + '}';
    }

}

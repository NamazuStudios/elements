package dev.getelements.elements.sdk.model.auth;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Internal, DAO-level model of a pending "password verified, awaiting TOTP code" login challenge. Not part of
 * the public REST API. Deliberately does not carry a {@link dev.getelements.elements.sdk.model.session.Session}
 * or any session secret -- only the inputs needed to resume session creation once the code is verified, so no
 * session material exists anywhere until authentication actually completes.
 */
public class TotpLoginChallenge {

    /** Creates a new instance. */
    public TotpLoginChallenge() {}

    private String id;

    private String userId;

    private String profileId;

    private String profileSelector;

    private String applicationNameOrId;

    private Timestamp expiry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileSelector() {
        return profileSelector;
    }

    public void setProfileSelector(String profileSelector) {
        this.profileSelector = profileSelector;
    }

    public String getApplicationNameOrId() {
        return applicationNameOrId;
    }

    public void setApplicationNameOrId(String applicationNameOrId) {
        this.applicationNameOrId = applicationNameOrId;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TotpLoginChallenge that = (TotpLoginChallenge) o;
        return Objects.equals(id, that.id)
                && Objects.equals(userId, that.userId)
                && Objects.equals(profileId, that.profileId)
                && Objects.equals(profileSelector, that.profileSelector)
                && Objects.equals(applicationNameOrId, that.applicationNameOrId)
                && Objects.equals(expiry, that.expiry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, profileId, profileSelector, applicationNameOrId, expiry);
    }

    @Override
    public String toString() {
        return "TotpLoginChallenge{id='" + id + "', userId='" + userId + "'}";
    }

}

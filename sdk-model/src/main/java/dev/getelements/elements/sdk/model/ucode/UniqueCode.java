package dev.getelements.elements.sdk.model.ucode;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * Represents a unique code with an identifier, the code itself, and an expiration timestamp.
 */
@Schema(description = "Represents a unique code with an identifier, the code itself, and an expiration timestamp.")
public class UniqueCode {

    private String id;

    private String code;

    private long linger;

    private long timeout;

    private long expiresAt;

    private User user;

    private Profile profile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getLinger() {
        return linger;
    }

    public void setLinger(long linger) {
        this.linger = linger;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        UniqueCode that = (UniqueCode) object;
        return linger == that.linger && timeout == that.timeout && expiresAt == that.expiresAt && Objects.equals(id, that.id) && Objects.equals(code, that.code) && Objects.equals(user, that.user) && Objects.equals(profile, that.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, linger, timeout, expiresAt, user, profile);
    }

    @Override
    public String toString() {
        return "UniqueCode{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", linger=" + linger +
                ", timeout=" + timeout +
                ", expiresAt=" + expiresAt +
                ", user=" + user +
                ", profile=" + profile +
                '}';
    }

}

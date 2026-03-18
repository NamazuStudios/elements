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

    /** Creates a new instance. */
    public UniqueCode() {}

    private String id;

    private long linger;

    private long timeout;

    private long expiry;

    private User user;

    private Profile profile;

    /**
     * Returns the unique ID of this code.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of this code.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the linger duration for this code in milliseconds.
     *
     * @return the linger duration
     */
    public long getLinger() {
        return linger;
    }

    /**
     * Sets the linger duration for this code in milliseconds.
     *
     * @param linger the linger duration
     */
    public void setLinger(long linger) {
        this.linger = linger;
    }

    /**
     * Returns the timeout duration for this code in milliseconds.
     *
     * @return the timeout duration
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout duration for this code in milliseconds.
     *
     * @param timeout the timeout duration
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the expiry time for this code in milliseconds since Unix epoch.
     *
     * @return the expiry time
     */
    public long getExpiry() {
        return expiry;
    }

    /**
     * Sets the expiry time for this code in milliseconds since Unix epoch.
     *
     * @param expiry the expiry time
     */
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    /**
     * Returns the user associated with this code.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this code.
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the profile associated with this code.
     *
     * @return the profile
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * Sets the profile associated with this code.
     *
     * @param profile the profile
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        UniqueCode that = (UniqueCode) object;
        return linger == that.linger && timeout == that.timeout && expiry == that.expiry && Objects.equals(id, that.id) && Objects.equals(user, that.user) && Objects.equals(profile, that.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, linger, timeout, expiry, user, profile);
    }

    @Override
    public String toString() {
        return "UniqueCode{" +
                "id='" + id + '\'' +
                ", linger=" + linger +
                ", timeout=" + timeout +
                ", expiresAt=" + expiry +
                ", user=" + user +
                ", profile=" + profile +
                '}';
    }

}

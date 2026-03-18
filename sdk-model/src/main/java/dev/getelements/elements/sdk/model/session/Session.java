package dev.getelements.elements.sdk.model.session;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Represents a session authorized by Elements.  This includes the associated {@link User}, {@link Profile}, and
 * {@link Application}.  This has no ID, as the ID is sensitive information.  The only time a key for the
 * {@link Session} is provided is through a the {@link SessionCreation#getSessionSecret()}.  The actual ID of the
 * session is hashed in the database and should only be kept on the client.
 *
 * Created by patricktwohig on 6/22/17.
 */
@Schema
public class Session implements Serializable {

    /** Creates a new instance. */
    public Session() {}

    @NotNull
    private User user;

    private Profile profile;

    private Application application;

    private long expiry;

    /**
     * Returns the user associated with this session.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this session.
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the profile associated with this session.
     *
     * @return the profile
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * Sets the profile associated with this session.
     *
     * @param profile the profile
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    /**
     * Returns the application associated with this session.
     *
     * @return the application
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Sets the application associated with this session.
     *
     * @param application the application
     */
    public void setApplication(Application application) {
        this.application = application;
    }

    /**
     * Returns the expiry time of this session in milliseconds since Unix epoch.
     *
     * @return the expiry time
     */
    public long getExpiry() {
        return expiry;
    }

    /**
     * Sets the expiry time of this session in milliseconds since Unix epoch.
     *
     * @param expiry the expiry time
     */
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session)) return false;

        Session session = (Session) o;

        if (getExpiry() != session.getExpiry()) return false;
        if (getUser() != null ? !getUser().equals(session.getUser()) : session.getUser() != null) return false;
        if (getProfile() != null ? !getProfile().equals(session.getProfile()) : session.getProfile() != null)
            return false;
        return getApplication() != null ? getApplication().equals(session.getApplication()) : session.getApplication() == null;
    }

    @Override
    public int hashCode() {
        int result = getUser() != null ? getUser().hashCode() : 0;
        result = 31 * result + (getProfile() != null ? getProfile().hashCode() : 0);
        result = 31 * result + (getApplication() != null ? getApplication().hashCode() : 0);
        result = 31 * result + (int) (getExpiry() ^ (getExpiry() >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Session{" +
                "user=" + user +
                ", profile=" + profile +
                ", application=" + application +
                ", expiry=" + expiry +
                '}';
    }

    /**
     * Used as the key for the user attribute where appropriate.  This is equivalent
     * to the FQN of the {@link Session} class.
     */
    public static final String SESSION_ATTRIBUTE = Session.class.getName();

}

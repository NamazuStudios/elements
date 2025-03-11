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

    @NotNull
    private User user;

    private Profile profile;

    private Application application;

    private long expiry;

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

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public long getExpiry() {
        return expiry;
    }

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

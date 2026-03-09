package dev.getelements.elements.sdk.model.largeobject;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

/** Represents the set of subjects (users, profiles, or wildcard) permitted to perform operations on a large object. */
@Schema
public class Subjects {

    /** Creates a new instance. */
    public Subjects() {}

    @Schema(description = "Flag to check who may perform the operations. True if all users may access the object.")
    private boolean wildcard;

    @NotNull
    @Schema(description = "Users which may perform the operations.")
    private List<User> users;

    @NotNull
    @Schema(description = "Profiles, which owners may perform the operations.")
    private List<Profile> profiles;

    @Schema(description = "Specifies a minimum access level, if applicable.")
    private User.Level minimumLevel;

    /**
     * Fetches an anonymous subject which permits no access.
     * @return a new {@link Subjects} object.
     */
    public static Subjects nobody() {
        Subjects anonymousSubject = new Subjects();
        anonymousSubject.setWildcard(false);
        anonymousSubject.setUsers(emptyList());
        anonymousSubject.setProfiles(emptyList());
        return anonymousSubject;
    }

    /**
     * Fetches an anonymous subject which permits all access.
     * @return a new {@link Subjects} object.
     */
    public static Subjects wildcardSubject() {
        Subjects anonymousSubject = new Subjects();
        anonymousSubject.setWildcard(true);
        anonymousSubject.setUsers(emptyList());
        anonymousSubject.setProfiles(emptyList());
        return anonymousSubject;
    }

    /**
     * Specifies a minimum access level.
     ** @param minimumLevel the minimum access level
     *
     * @return a new {@link Subjects} with the specified minimum access level.
     */
    public static Subjects withMinimumLevel(final User.Level minimumLevel) {
        Subjects anonymousSubject = new Subjects();
        anonymousSubject.setWildcard(false);
        anonymousSubject.setMinimumLevel(minimumLevel);
        anonymousSubject.setUsers(emptyList());
        anonymousSubject.setProfiles(emptyList());
        return anonymousSubject;
    }

    /**
     * Returns whether this subjects instance is a wildcard that allows all users.
     *
     * @return true if wildcard
     */
    public boolean isWildcard() {
        return wildcard;
    }

    /**
     * Sets whether this subjects instance is a wildcard that allows all users.
     *
     * @param wildcard true if wildcard
     */
    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    /**
     * Returns the users permitted to perform operations.
     *
     * @return the users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Sets the users permitted to perform operations.
     *
     * @param users the users
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Returns the profiles whose owners are permitted to perform operations.
     *
     * @return the profiles
     */
    public List<Profile> getProfiles() {
        return profiles;
    }

    /**
     * Sets the profiles whose owners are permitted to perform operations.
     *
     * @param profiles the profiles
     */
    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    /**
     * Returns the minimum access level required to perform operations.
     *
     * @return the minimum level
     */
    public User.Level getMinimumLevel() {
        return minimumLevel;
    }

    /**
     * Sets the minimum access level required to perform operations.
     *
     * @param minimumLevel the minimum level
     */
    public void setMinimumLevel(User.Level minimumLevel) {
        this.minimumLevel = minimumLevel;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Subjects subjects = (Subjects) object;
        return wildcard == subjects.wildcard && Objects.equals(users, subjects.users) && Objects.equals(profiles, subjects.profiles) && minimumLevel == subjects.minimumLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(wildcard, users, profiles, minimumLevel);
    }

    @Override
    public String toString() {
        return "Subjects{" +
                "wildcard=" + wildcard +
                ", users=" + users +
                ", profiles=" + profiles +
                ", minimumLevel=" + minimumLevel +
                '}';
    }

}

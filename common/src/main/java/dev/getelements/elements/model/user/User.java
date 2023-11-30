package dev.getelements.elements.model.user;

import dev.getelements.elements.Constants;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.profile.Profile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.model.ValidationGroups.*;

/**
 * Represents a user in the system.  Users are differing from entrants in that they are active users
 * who have special privilege to create/manage content in the system.
 *
 * Created by patricktwohig on 3/19/15.
 */
@ApiModel
public class User implements Serializable {

    @Null(groups = Insert.class)
    @NotNull(groups = {Update.class, Read.class})
    @ApiModelProperty("The user's database assigned unique ID.")
    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    @ApiModelProperty("A unique name for the user.")
    private String name;

    @Pattern(regexp = Constants.Regexp.FIRST_NAME)
    @ApiModelProperty("The user's first name")
    private String firstName;

    @Pattern(regexp = Constants.Regexp.LAST_NAME)
    @ApiModelProperty("The user's last name")
    private String lastName;

    @NotNull
    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    @ApiModelProperty("The user's email.")
    private String email;

    @Pattern(regexp = Constants.Regexp.PHONE_NB)
    @ApiModelProperty("The user's phone number.")
    private String primaryPhoneNb;

    @NotNull
    @ApiModelProperty("The user's access level.")
    private Level level;

    @ApiModelProperty("True if the user is active. False otherwise.")
    private boolean active;

    @ApiModelProperty("The user's Facebook ID.")
    private String facebookId;

    @ApiModelProperty("The user's Firebase ID.")
    private String firebaseId;

    @ApiModelProperty("The user's Apple Sign-In ID.")
    private String appleSignInId;

    @ApiModelProperty("The user's Google Sign-In ID.")
    private String googleSignInId;

    @ApiModelProperty("The user's external user ID. Used for custom authorization.")
    private String externalUserId;

    private static final User UNPRIVILIGED = new User() {

        @Override
        public String getEmail() {
            return "";
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getPrimaryPhoneNb() {
            return "";
        }

        @Override
        public Level getLevel() {
            return Level.UNPRIVILEGED;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public String getFirebaseId() {
            return null;
        }

        @Override
        public String getFacebookId() {
            return null;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return  obj == this;
        }

    };

    /**
     * Gets the user's unique ID.
     *
     * @return the user's unique ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the user.
     *
     * @param id the user's unique ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the user's login name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's login name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's access level.
     * @return
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Sets the user's access level.
     *
     * @param level
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * Returns true if the user is active.
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the user active flag.
     *
     * @param active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the user's facebook id, if present.  If a user is not linked to
     * a Facebook account then this will simply be null.
     *
     * @return the user's facebook ID
     */
    public String getFacebookId() {
        return facebookId;
    }

    /**
     * Sets a user's facebook id.
     *
     * @param facebookId the user's facebook id
     */
    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    /**
     * Gets the user's firebase ID.
     *
     * @return the user's firebase ID.
     */
    public String getFirebaseId() {
        return firebaseId;
    }

    /**
     * Sets the user's firebase ID.
     *
     * @param firebaseId
     */
    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    /**
     * Gets the Apple sign-in ID.
     *
     * @return the apple sign-in id
     */
    public String getAppleSignInId() {
        return appleSignInId;
    }

    /**
     *
     * @param appleSignInId
     */
    public void setAppleSignInId(String appleSignInId) {
        this.appleSignInId = appleSignInId;
    }

    /**
     * Gets the Google sign-in ID.
     *
     * @return the google sign-in id
     */
    public String getGoogleSignInId() {
        return googleSignInId;
    }

    /**
     * Sets the user's Google sign-in ID.
     *
     * @param googleSignInId
     */
    public void setGoogleSignInId(String googleSignInId) {
        this.googleSignInId = googleSignInId;
    }

    /**
     * Gets the user's external user ID.
     *
     * @return the user's external user ID.
     */
    public String getExternalUserId() {
        return externalUserId;
    }

    /**
     * Set the user's external user ID.
     *
     * @param externalUserId the external user ID
     */
    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    /**
     * Gets a special User object which is set to unprivileged.  This is used
     * as a palceholder when a user is not logged in.
     *
     * This is a singleton object.
     *
     * @return an User with the UNPRIVILEGED state set
     */
    public static User getUnprivileged() {
        return UNPRIVILIGED;
    }

    /**
     * Used as the key for the user attribute where appropriate.  This is equivalent
     * to the FQN of the {@link User} class.
     */
    public static final String USER_ATTRIBUTE = User.class.getName();

    public String getPrimaryPhoneNb() {
        return primaryPhoneNb;
    }

    public void setPrimaryPhoneNb(String primaryPhoneNb) {
        this.primaryPhoneNb = primaryPhoneNb;
    }

    public enum Level {

        /**
         * An unprivileged/anonymous user.
         */
        UNPRIVILEGED,

        /**
         * A basic user.
         */
        USER,

        /**
         * An administrator/super user, who can do all of the above as well
         * as delete/create users.
         */
        SUPERUSER,

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return active == user.active && Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(email, user.email) && Objects.equals(primaryPhoneNb, user.primaryPhoneNb) && level == user.level && Objects.equals(facebookId, user.facebookId) && Objects.equals(firebaseId, user.firebaseId) && Objects.equals(appleSignInId, user.appleSignInId) && Objects.equals(externalUserId, user.externalUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, firstName, lastName, email, primaryPhoneNb, level, active, facebookId, firebaseId, appleSignInId, externalUserId);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", primaryPhoneNb='" + primaryPhoneNb + '\'' +
                ", level=" + level +
                ", active=" + active +
                ", facebookId='" + facebookId + '\'' +
                ", firebaseId='" + firebaseId + '\'' +
                ", appleSignInId='" + appleSignInId + '\'' +
                ", googleSignInId='" + googleSignInId + '\'' +
                ", externalUserId='" + externalUserId + '\'' +
                '}';
    }
}

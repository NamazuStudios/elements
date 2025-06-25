package dev.getelements.elements.sdk.model.user;

import dev.getelements.elements.sdk.model.Constants;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import static dev.getelements.elements.sdk.model.ValidationGroups.*;

/**
 * Represents a user in the system.  Users are differing from entrants in that they are active users
 * who have special privilege to create/manage content in the system.
 *
 * Created by patricktwohig on 3/19/15.
 */
@Schema
public class User implements Serializable {

    @Null(groups = Insert.class)
    @NotNull(groups = {Update.class, Read.class})
    @Schema(description = "The user's database assigned unique ID.")
    private String id;

    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    @Schema(description = "A unique name for the user.")
    private String name;

    @Pattern(regexp = Constants.Regexp.FIRST_NAME)
    @Schema(description = "The user's first name")
    private String firstName;

    @Pattern(regexp = Constants.Regexp.LAST_NAME)
    @Schema(description = "The user's last name")
    private String lastName;

    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    @Schema(description = "The user's email.")
    private String email;

    @Pattern(regexp = Constants.Regexp.PHONE_NB)
    @Schema(description = "The user's phone number.")
    private String primaryPhoneNb;

    @NotNull
    @Schema(description = "The user's access level.")
    private Level level;

    @Schema(description = "List of linked account or auth scheme names.")
    private Set<String> linkedAccounts;

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
     * Gets a special User object which is set to unprivileged.  This is used
     * as a palceholder when a user is not logged in.
     *
     * This is a singleton object.
     *
     * @return a User with the UNPRIVILEGED state set
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

    public Set<String> getLinkedAccounts() {
        return linkedAccounts;
    }

    public void setLinkedAccounts(Set<String> linkedAccounts) {
        this.linkedAccounts = linkedAccounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(email, user.email) && Objects.equals(primaryPhoneNb, user.primaryPhoneNb) && level == user.level && Objects.equals(linkedAccounts, user.linkedAccounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, firstName, lastName, email, primaryPhoneNb, level, linkedAccounts);
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
                ", linkedAccounts=" + linkedAccounts +
                '}';
    }

}

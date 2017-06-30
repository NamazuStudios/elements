package com.namazustudios.socialengine.model;

import com.namazustudios.socialengine.Constants;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Represents a user in the system.  Users are differing from entrants in that they are active users
 * who have special privilege to create/manage content in the system.
 *
 * Created by patricktwohig on 3/19/15.
 */
@ApiModel
public class User {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NON_BLANK_STRING)
    private String name;

    @NotNull
    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    private String email;

    @NotNull
    private Level level;

    private boolean active;

    private String facebookId;

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
        public Level getLevel() {
            return Level.UNPRIVILEGED;
        }

        @Override
        public boolean isActive() {
            return false;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (isActive() != user.isActive()) return false;
        if (getName() != null ? !getName().equals(user.getName()) : user.getName() != null) return false;
        if (getEmail() != null ? !getEmail().equals(user.getEmail()) : user.getEmail() != null) return false;
        if (getLevel() != user.getLevel()) return false;
        return getFacebookId() != null ? getFacebookId().equals(user.getFacebookId()) : user.getFacebookId() == null;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getEmail() != null ? getEmail().hashCode() : 0);
        result = 31 * result + (getLevel() != null ? getLevel().hashCode() : 0);
        result = 31 * result + (isActive() ? 1 : 0);
        result = 31 * result + (getFacebookId() != null ? getFacebookId().hashCode() : 0);
        return result;
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

}

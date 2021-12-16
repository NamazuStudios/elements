package com.namazustudios.socialengine.model.user;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a user claim from a JWT request.
 */
public class UserClaim implements Serializable {

    private String name;

    private String email;

    private User.Level level;

    private String facebookId;

    private String firebaseId;

    private String appleSignInId;

    private List<String> externalUserIds;

    /**
     * Gets the user's login name.
     *
     * @return name
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
    public User.Level getLevel() {
        return level;
    }

    /**
     * Sets the user's access level.
     *
     * @param level
     */
    public void setLevel(User.Level level) {
        this.level = level;
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
     * Sets the Apple sign-in ID
     *
     * @param appleSignInId
     */
    public void setAppleSignInId(String appleSignInId) {
        this.appleSignInId = appleSignInId;
    }

    /**
     * Gets the list of external user ids
     *
     * @return externalUserIds
     */
    public List<String> getExternalUserIds() {
        return externalUserIds;
    }

    /**
     * Sets the list of external user ids
     *
     * @return externalUserIds
     */
    public void setExternalUserIds(List<String> externalUserIds) {
        this.externalUserIds = externalUserIds;
    }
}

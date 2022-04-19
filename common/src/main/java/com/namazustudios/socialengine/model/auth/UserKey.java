package com.namazustudios.socialengine.model.auth;

import com.namazustudios.socialengine.model.user.User;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Enumerates the user keys. This is used in conjunction with the {@link PrivateClaim#USER_KEY} and represents all valid
 * values for that claim.
 */
public enum UserKey {

    /**
     * The user name. Maps to {@link User#getName()}.
     */
    NAME("name"),

    /**
     * The user email. Maps to {@link User#getEmail()}.
     */
    EMAIL("email"),

    /**
     * The user's Facebook ID. Maps to {@link User#getFacebookId()}
     */
    FACEBOOK_ID("facebookId"),

    /**
     * The user's Facebook ID. Maps to {@link User#getFacebookId()}
     */
    FIREBASE_ID("firebaseId"),

    /**
     * The user's Apple Sign-In Id. Maps to {@link User#getAppleSignInId()}.
     */
    APPLE_SIGN_IN_ID("appleSignInId"),

    /**
     * The user's external user id. Maps to {@link User#getExternalUserId()}.
     */
    EXTERNAL_USER_ID("externalUserId");

    private final String value;

    UserKey(String value) {
        this.value = value;
    }

    /**
     * The literal value of the claim.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Finds the {@link UserKey} by the specified value.
     *
     * @param value the value
     * @return the value
     */
    public static Optional<UserKey> findByValue(final String value) {
        return Stream.of(values())
            .filter(e -> e.value.equals(value))
            .findFirst();
    }

}

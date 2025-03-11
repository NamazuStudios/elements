package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.user.User;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Enumerates the user keys. This is used in conjunction with the {@link PrivateClaim#USER_KEY} and represents all valid
 * values for that claim.
 */
public enum UserKey {

    /**
     * The user's name. Maps to {@link User#getName()}.
     */
    NAME("name"),

    /**
     * The user email. Maps to {@link User#getEmail()}.
     */
    EMAIL("email"),

    /**
     * The user's external user id.
     *
     * @deprecated no longer in use.
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

package dev.getelements.elements.sdk.model.friend;

/**
 * Represents the type of relationship the user has with another user.
 */
public enum Friendship {

    /**
     * No friendship whatsoever.
     */
    NONE,

    /**
     * The user has requested friendship from the other user, but the other user has not accepted the request for
     * friendship.
     */
    OUTGOING,

    /**
     * The other user has requested friendshipb, this user has not accepted the friendship.
     */
    INCOMING,

    /**
     * Both users have accepted friendship.
     */
    MUTUAL

}

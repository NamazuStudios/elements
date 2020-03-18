package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.UserCreateRequest;

import java.util.Objects;

/**
 * Created by patricktwohig on 3/19/15.
 */
public interface UserService {

    String CURRENT_USER_ALIAS = "me";

    /**
     * Gets the currently logged in user.
     *
     * @return the currently logged in user
     */
    User getCurrentUser();

    /**
     * Returns true if the specified user ID is the alias for the current user.
     *
     * @param userId the userId as returned by {@link User#getId()}
     * @return true if alias, false otherwise
     */
    default boolean isCurrentUserAlias(final String userId) {
        return CURRENT_USER_ALIAS.equals(userId);
    }

    /**
     * Given the userId, which may be either username, email address, or "me" this will check for the current user.
     *
     * @param userId the userId, which could be either email, name, "me", or null
     * @return true if matches current user, false otherwise
     */
    default boolean isCurrentUser(final String userId) {
        return isCurrentUserAlias(userId) || Objects.equals(getCurrentUser().getId(), userId);
    }

    /**
     * Given the userId, which may be either username or email address,
     * will check for the current user.  If the current user does not match,
     * then this throws an exception to indicate that the current user does not
     * match.
     *
     * @param userId the userId, which could be either email or name
     * @throws com.namazustudios.socialengine.exception.NotFoundException if the current user does not match
     */
    default void checkForCurrentUser(final String userId) {
        if (!isCurrentUser(userId)) throw new NotFoundException("User with id " + userId + " not found.");
    }

    /**
     * Gets a user with unique user ID.
     *
     * @param userId the UserId
     *
     * @return the user ID
     */
    User getUser(final String userId);

    /**
     * Gets a list of users the current user can see.
     *
     * @param offset the offset
     * @param count the count
     * @return the PaginatedEntry of users
     */
    Pagination<User> getUsers(int offset, int count);

    /**
     * Gets a list of users the current user can see.
     *
     * @param offset the offset
     * @param count the count
     * @param search the search query
     *
     * @return the PaginatedEntry of users
     */
    Pagination<User> getUsers(int offset, int count, String search);

    /**
     * Creates a new user.
     *
     * @param user the user to create
     * @return the User, as it was create
     */
    User createUser(final User user);

    /**
     * Creates a user with the given password.  The password must be non-null, non-empty
     *
     * @param user the User object
     * @param password the newly created User's password
     * @return the User, as it was created in the database
     */
    User createUser(final User user, final String password);

    /**
     * Signs up a new user.
     *
     * @param userCreateRequest the details to use when creating the user
     * @return the User, as it was create
     */
    User createUser(final UserCreateRequest userCreateRequest);

    /**
     * Updates a user.
     *
     * @param user the user to update
     * @return the User, as it was updated
     */
    User updateUser(final User user);

    /**
     * Updates a user.
     *
     * @param user the user to update
     * @param password the user's password
     * @return the User, as it was updated
     */
    User updateUser(final User user, final String password);

    /**
     * Removes a user from the system, effectively deleting his/her account.
     *
     * @param userId the userId
     */
    void deleteUser(final String userId);

}

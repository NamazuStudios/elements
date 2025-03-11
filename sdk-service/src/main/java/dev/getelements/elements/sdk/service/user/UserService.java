package dev.getelements.elements.sdk.service.user;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.*;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Objects;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Service to access Users in the system.
 *
 * Created by patricktwohig on 3/19/15.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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
     * @throws NotFoundException if the current user does not match
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
     * Creates a new user.  The service may override or reject the request based on the current user access level.
     *
     * @param userCreateRequest the user to create
     * @return the User, as it was created by the database
     */
    UserCreateResponse createUser(UserCreateRequest userCreateRequest);

    /**
     * Updates a user, preserving the user's password.
     *
     * @param userId  the user ID to update
     * @param userUpdateRequest the user to update
     * @return the User, as it was updated
     */
    User updateUser(String userId, UserUpdateRequest userUpdateRequest);

    /**
     * Updates the User's password given the {@link UserUpdatePasswordRequest} and the user ID.
     * @param userId the user ID to update
     * @param userUpdatePasswordRequest the {@link UserUpdatePasswordRequest} instance
     * @return a {@link SessionCreation} indicating new session key
     */
    SessionCreation updateUserPassword(String userId, UserUpdatePasswordRequest userUpdatePasswordRequest);

    /**
     * Removes a user from the system, effectively deleting his/her account.
     *
     * @param userId the userId
     */
    void deleteUser(final String userId);

}

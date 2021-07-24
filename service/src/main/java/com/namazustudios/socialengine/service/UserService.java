package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.user.UserCreateRequest;
import com.namazustudios.socialengine.model.user.UserUpdateRequest;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.Objects;

/**
 * Service to access Users in the system.
 *
 * Created by patricktwohig on 3/19/15.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.user"),
    @ModuleDefinition(
        value = "namazu.elements.service.unscoped.user",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
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
     * Creates a new user.  The service may override or reject the request based on the current user access level.
     *
     * @param userCreateRequest the user to create
     * @return the User, as it was created by the database
     */
    User createUser(UserCreateRequest userCreateRequest);

    /**
     * Updates a user, preserving the user's password.
     *
     * @param userId  the user ID to update
     * @param userUpdateRequest the user to update
     * @return the User, as it was updated
     */
    User updateUser(String userId, UserUpdateRequest userUpdateRequest);

    /**
     * Removes a user from the system, effectively deleting his/her account.
     *
     * @param userId the userId
     */
    void deleteUser(final String userId);

}

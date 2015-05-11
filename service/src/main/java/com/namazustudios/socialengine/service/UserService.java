package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;

/**
 * Created by patricktwohig on 3/19/15.
 */
public interface UserService {

    /**
     * Gets the currently logged in user.
     *
     * @return the currently logged in user
     */
    User getCurrentUser();

    /**
     * Given the userId, which may be either username or email address,
     * will check for the current user.  If the current user does not match,
     * then this throws an exception to indicate that the current user does not
     * match.s
     *
     * @param userId the userId, which could be either email or name
     * @throws com.namazustudios.socialengine.exception.NotFoundException if the current user does not match
     */
    void checkForCurrentUser(final String userId);

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

    /**
     * UPdates the password for a given user.
     *
     * @param userId the user's name
     * @param password the user's password
     *
     * @return the user.
     */
    User updateUserPassword(final String userId, final String password);

}

package com.namazustudios.promotion.service;

import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.User;

/**
 * Created by patricktwohig on 3/19/15.
 */
public interface UserService {

    /**
     * Gets the currently logged in user.
     *
     * @return the currently logged in user
     */
    public User getCurrentUser();

    /**
     * Given the userId, which may be either username or email address,
     * will check for the current user.  If the current user does not match,
     * then this throws an exception to indicate that the current user does not
     * match.s
     *
     * @param userId the userId, which could be either email or name
     * @throws com.namazustudios.promotion.exception.NotFoundException if the current user does not match
     */
    public void checkForCurrentUser(final String userId);

    /**
     * Gets a user with unique user ID.
     *
     * @param userId the UserId
     *
     * @return the user ID
     */
    public User getUser(final String userId);

    /**
     * Gets a list of users the current user can see.
     *
     * @param offset the offset
     * @param count the count
     * @return the PaginatedEntry of users
     */
    public Pagination<User> getUsers(int offset, int count);

    /**
     * Creates a new user.
     *
     * @param user the user to create
     * @return the User, as it was create
     */
    public User createUser(final User user);

    /**
     * Updates a user.
     *
     * @param user the user to update
     * @return the User, as it was updated
     */
    public User updateUser(final User user);

    /**
     * Removes a user from the system, effectively deleting his/her account.
     *
     * @param userId the userId
     */
    public void deleteUser(final String userId);

    /**
     * UPdates the password for a given user.
     *
     * @param userId the user's name
     * @param password the user's password
     *
     * @return the user.
     */
    public User updateUserPassword(final String userId, final String password);

}

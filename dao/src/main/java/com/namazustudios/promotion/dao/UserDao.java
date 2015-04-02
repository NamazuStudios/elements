package com.namazustudios.promotion.dao;

import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.User;

/**
 * Created by patricktwohig on 3/26/15.
 */
public interface UserDao {

    /**
     * Gets the user with the userId, which may be either email addres or name.
     *
     * @param userId
     * @return
     */
    public User getUser(String userId);

    /**
     * Gets a listing of all users given the offset, and count.
     *
     * @param offset the offset
     * @param count the count
     * @return the users in the system
     */
    public Pagination<User> getUsers(int offset, int count);

    /**
     * Creates a user with the given User object.
     *
     * @param user the user to create
     *
     * @return the User as it was created.
     */
    public User createUser(final User user);

    /**
     * Writes the user to the database, and returns the result
     * of how the user was written to the databse
     *
     * @param user
     * @return
     */
    public User updateUser(final User user);

    /**
     * Deletes a user from the databse.  In actuality, this isn't a true delete, but
     * rather just flags the user as inactive.
     *
     * @param userId the user's id (name or email address)
     */
    public void deleteUser(final String userId);

    /**
     * Updates the user's password adn returns the user object.
     *
     * @param userId the userId of the user (which may be email or name)
     * @return the udpated user object
     */
    public User updateUserPassword(final String userId, final String password);

    /**
     * Validates the user's password and returns the current User instance.  If the password validation fails,
     * then this simply throws an instance of {@link com.namazustudios.promotion.exception.ForbiddenException}
     *
     * @param userId the userId
     * @param password the password
     *
     * @return the User, never null
     */
    public User validateUserPassword(final String userId, final String password);

}

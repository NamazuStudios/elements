package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;

/**
 * Created by patricktwohig on 3/26/15.
 */
public interface UserDao {

    /**
     * Gets the user with the userId, which may be either email address or name.  Additionally,
     * the user requested must be active.
     *
     * @param userId
     * @return
     */
    public User getUser(String userId);

    /**
     * Gets a listing of all users given the offset, and count.  Additionally, the
     * user requested must be active.
     *
     * @param offset the offset
     * @param count the count
     * @return the users in the system
     */
    public Pagination<User> getUsers(int offset, int count);

    /**
     * Creates a user with the given User object.  If the user exists
     * then this will re-enstate the user's account.
     *
     * @param user the user to create
     *
     * @return the User as it was created.
     */
    public User createUser(final User user);

    /**
     * Creates a user and sets the user's password, atomically.  If the user exists
     * then this will reinstate the user's account with a new password.
     *
     * When calling this, the user,
     *
     * @param user the user to create
     * @param password the password for the user to use
     *
     * @return the User, as was written to the database
     */
    public User createUser(final User user, final String password);

    /**
     * Updates the given user, regardless of active status and then returns
     * the user instance as it was written to the database.
     *
     * The given {@link com.namazustudios.socialengine.model.User#isActive()} is only honored for this,
     * call if setting to true.  This cannot be used to deactivate a user, if wishing to set
     * a user as inactive please use the {@link #softDeleteUser(String)} instead.
     *
     * @param user the user to update
     * @return the user as was written to the database
     */
    public User updateUser(User user);

    /**
     * Updates the given active user.  If the user has been deleted or has been
     * flagged as inactive, then this method will fail.
     *
     * The given {@link com.namazustudios.socialengine.model.User#isActive()} is ignored for this,
     * if wishing to set a user as inactive, please use the {@link #softDeleteUser(String)} instead.
     *
     * @param user the user to update
     * @return
     */
    public User updateActiveUser(final User user);

    /**
     * Deletes a user from the database.  In actuality, this isn't a true delete, but
     * rather just flags the user as inactive.  Once flagged inactive, a user will
     * not show up in any results for active users.
     *
     * @param userId the user's id (name or email address)
     */
    public void softDeleteUser(final String userId);

    /**
     * Updates the user's password and returns the user object.
     *
     * @param userId the userId of the user (which may be email or name)
     * @return the udpated user object
     */
    public User updateUserPassword(final String userId, final String password);

    /**
     * Validates the user's password and returns the current User instance.  If the password validation fails,
     * then this simply throws an instance of {@link com.namazustudios.socialengine.exception.ForbiddenException}
     *
     * @param userId the userId
     * @param password the password
     *
     * @return the User, never null
     */
    public User validateUserPassword(final String userId, final String password);

}

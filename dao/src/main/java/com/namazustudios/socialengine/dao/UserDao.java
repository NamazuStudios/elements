package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;

/**
 * This is the UserDao which is used to update users in the database.  Since users
 * can remain long after deletion, this has several methods which behave slightly
 * differently.
 *
 * Generally methods with the word "strict" in their prototype mean the operation
 * without regard for the active flag.  Other methods may use the active flag to
 * emulate a user that has been deleted from the system.
 *
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
    User getActiveUser(String userId);

    /**
     * Gets a listing of all users given the offset, and count.  Additionally, the
     * user requested must be active.
     *
     * @param offset the offset
     * @param count the count
     * @return the users in the system
     */
    Pagination<User> getActiveUsers(int offset, int count);

    /**
     * Gets a listing of all users given the offset, and count.  Additionally, the
     * user requested must be active.
     *
     * @param offset the offset
     * @param count the count
     * @param query a query to filter the results
     * @return the users in the system
     */
    Pagination<User> getActiveUsers(int offset, int count, String query);


    /**
     * Creates a user with the given User object.  Using "Strict" semantics, if the user exists
     * then this will throw an exception.  The resulting user will have a scrambled password.
     *
     * @param user the user to create
     *
     * @return the User as it was created.
     */
    User createUserStrict(final User user);

    /**
     * Creates a user with the given User object and password.  Using "Strict" semantics, if the
     * user exists then this will throw an exception.  The resulting user will be assigned
     * the given password.
     *
     * @param user
     * @param password
     * @return
     */
    User createUserStrict(final User user, final String password);

    /**
     * Creates or activates a user, or if the user is currently inactive
     * this will reinstate access.  This securely scrambles the user's password
     * and therefore the user must change password at a later date.
     *
     * @param user the user
     * @return the User, as written to the database
     */
    User createOrActivateUser(final User user);

    /**
     * Creates a user and sets the user's password.  If the user exists
     * then this will reinstate the user's account with a new password.
     *
     * @param user the user to create
     * @param password the password for the user to use
     *
     * @return the User, as was written to the database
     */
    User createOrActivateUser(final User user, final String password);

    /**
     * Updates the given user, regardless of active status and then returns
     * the user instance as it was written to the database.
     *
     * The given {@link com.namazustudios.socialengine.model.User#isActive()} is only honored for this,
     * call if setting to true.  This cannot be used to deactivate a user, if wishing to set
     * a user as inactive please use the {@link #softDeleteUser(String)} instead.
     *
     * This does not change the user's password.
     *
     * @param user the user to update
     * @return the user as was written to the database
     */
    User updateUserStrict(final User user);

    /**
     * Updates the given user, regardless of active status and then returns
     * the user instance as it was written to the database.
     *
     * The given {@link com.namazustudios.socialengine.model.User#isActive()} is only honored for this,
     * call if setting to true.  This cannot be used to deactivate a user, if wishing to set
     * a user as inactive please use the {@link #softDeleteUser(String)} instead.
     *
     * This will update the user's password.
     *
     * @param user the user to update
     * @return the user as was written to the database
     */
    User updateUserStrict(User user, final String password);

    /**
     * Updates the given active user.  If the user has been deleted or has been
     * flagged as inactive, then this method will fail.
     *
     * The given {@link com.namazustudios.socialengine.model.User#isActive()} is ignored for this,
     * if wishing to set a user as inactive, please use the {@link #softDeleteUser(String)} instead.
     *
     * This does not change the user's password.
     *
     * @param user the user to update
     *
     * @return the User as written to the database
     */
    User updateActiveUser(final User user);

    /**
     * Updates the given active user.  If the user has been deleted or has been
     * flagged as inactive, then this method will fail.
     *
     * The given {@link com.namazustudios.socialengine.model.User#isActive()} is ignored for this,
     * if wishing to set a user as inactive, please use the {@link #softDeleteUser(String)} instead.
     *
     * @param user the user to update
     * @param password the user password
     *
     * @return the User, as written to the database
     */
    User updateActiveUser(final User user, final String password);

    /**
     * Deletes a user from the database.  In actuality, this isn't a true delete, but
     * rather just flags the user as inactive.  Once flagged inactive, a user will
     * not show up in any results for active users.
     *
     * @param userId the user's id (name or email address)
     */
    void softDeleteUser(final String userId);

    /**
     * Updates the user's password and returns the user object.
     *
     * @param userId the userId of the user (which may be email or name)
     * @return the udpated user object
     */
    User updateActiveUserPassword(final String userId, final String password);

    /**
     * Validates the user's password and returns the current User instance.  If the password validation fails,
     * then this simply throws an instance of {@link com.namazustudios.socialengine.exception.ForbiddenException}
     *
     * @param userId the userId
     * @param password the password
     *
     * @return the User, never null
     */
    User validateActiveUserPassword(final String userId, final String password);

}

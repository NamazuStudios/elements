package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.model.Pagination;

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
@Expose(modules = {
    "namazu.elements.dao.user",
    "namazu.socialengine.dao.user"
})
public interface UserDao {

    /**
     * Gets the user with the userId.  If the user is not active, then this method will behave as if the user does
     * not exist.
     *
     * @param userId the user's as determined by {@link User#getId()}
     *
     * @return the active user
     */
    User getActiveUser(String userId);

    /**
     * Gets the user with the user name or email address.  If the user is not active, then this method will behave as
     * if the user does not exist.
     *
     * @param userNameOrEmail the username or email
     * @return the active user
     */
    User getActiveUserByNameOrEmail(String userNameOrEmail);

    /**
     * Gets a listing of all users given the offset, and count.  Additionally, the user requested must be active.  Users
     * not active will be treated as if they do not exist.
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
    User createUserStrict(User user);

    /**
     * Creates a user with the given User object and password.  Using "Strict" semantics, if the
     * user exists then this will throw an exception.  The resulting user will be assigned
     * the given password.
     *
     * @param user
     * @param password
     * @return
     */
    User createUserWithPasswordStrict(User user, String password);

    /**
     * Creates or activates a user, or if the user is currently inactive
     * this will reinstate access.  This securely scrambles the user's password
     * and therefore the user must change password at a later date.
     *
     * Similar to {@link #createUserStrict(User)} the user will be assigned a scrambled
     * password if the user does not exist (or was previously inactive). This will not
     * touch the user's password if the user both exists and was flagged as active.
     *
     * @param user the user
     * @return the User, as written to the database
     */
    User createOrReactivateUser(User user);

    /**
     * Creates a user and sets the user's password.  If the user exists
     * then this will reinstate the user's account with a new password.
     *
     * @param user the user to create
     * @param password the password for the user to use
     *
     * @return the User, as was written to the database
     */
    User createOrReactivateUserWithPassword(User user, String password);

    /**
     * Updates the given user, regardless of active status and then returns
     * the user instance as it was written to the database.
     *
     * The given {@link User#isActive()} is only honored for this,
     * call if setting to true.  This cannot be used to deactivate a user, if wishing to set
     * a user as inactive please use the {@link #softDeleteUser(String)} instead.
     *
     * This does not change the user's password.
     *
     * @param user the user to update
     * @return the user as was written to the database
     */
    User updateUserStrict(User user);

    /**
     * Updates the given user, regardless of active status and then returns
     * the user instance as it was written to the database.
     *
     * The given {@link User#isActive()} is only honored for this,
     * call if setting to true.  This cannot be used to deactivate a user, if wishing to set
     * a user as inactive please use the {@link #softDeleteUser(String)} instead.
     *
     * This will update the user's password.
     *
     * @param user the user to update
     * @return the user as was written to the database
     */
    User updateUserStrict(User user, String password);

    /**
     * Updates the given active user.  If the user has been deleted or has been
     * flagged as inactive, then this method will fail.
     *
     * The given {@link User#isActive()} is ignored for this,
     * if wishing to set a user as inactive, please use the {@link #softDeleteUser(String)} instead.
     *
     * This does not change the user's password.
     *
     * @param user the user to update
     *
     * @return the User as written to the database
     */
    User updateActiveUser(User user);

    /**
     * Updates the given active user.  If the user has been deleted or has been
     * flagged as inactive, then this method will fail.
     *
     * The given {@link User#isActive()} is ignored for this,
     * if wishing to set a user as inactive, please use the {@link #softDeleteUser(String)} instead.
     *
     * @param user the user to update
     * @param password the user password
     *
     * @return the User, as written to the database
     */
    User updateActiveUser(User user, String password);

    /**
     * Deletes a user from the database.  In actuality, this isn't a true delete, but
     * rather just flags the user as inactive.  LazyValue flagged inactive, a user will
     * not show up in any results for active users.
     *
     * @param userId the user's as determined by {@link User#getId()}
     */
    void softDeleteUser(String userId);

    /**
     * Validates the user's password and returns the current User instance.  If the password validation fails,
     * then this simply throws an instance of {@link com.namazustudios.socialengine.exception.ForbiddenException}
     *
     * @param userNameOrEmail the user's name or email address
     * @param password the password
     *
     * @return the User, never null
     */
    User validateActiveUserPassword(String userNameOrEmail, String password);

}

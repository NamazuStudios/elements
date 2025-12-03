package dev.getelements.elements.sdk.dao;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.user.UserNotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;

import java.util.List;
import java.util.Optional;

/**
 * This is the UserDao which is used to update users in the database.  Since users
 * can remain long after deletion, this has several methods which behave slightly
 * differently.
 * <p>
 * Generally methods with the word "strict" in their prototype mean the operation
 * without regard for the active flag.  Other methods may use the active flag to
 * emulate a user that has been deleted from the system.
 * <p>
 * Created by patricktwohig on 3/26/15.
 */
@ElementServiceExport
public interface UserDao {

    /**
     * Gets the user with the userId.  If the user is not active, then this method will behave as if the user does
     * not exist.
     *
     * @param userId the user's as determined by {@link User#getId()}
     * @return the active user
     */
    User getUser(String userId);

    /**
     * Finds a user based on the supplied user id, returning the {@link Optional<User>} representing the resuot.
     *
     * @param userId the user's ID
     * @return an {@link Optional<User>}
     */
    default Optional<User> findUser(String userId) {
        try {
            return Optional.of(getUser(userId));
        } catch (UserNotFoundException unef) {
            return Optional.empty();
        }
    }

    /**
     * Finds a user either by email or name.
     *
     * @param userNameOrEmail the username or email
     * @return an {@link Optional<User>}
     */
    Optional<User> findUserByNameOrEmail(String userNameOrEmail);

    /**
     * Gets the user with the user name or email address.  If the user is not active, then this method will behave as
     * if the user does not exist.
     *
     * @param userNameOrEmail the username or email
     * @return the active user
     */
    default User getUserByNameOrEmail(final String userNameOrEmail) {
        return findUserByNameOrEmail(userNameOrEmail).orElseThrow(() -> {
            final String trimmedUserNameOrEmail = Strings.nullToEmpty(userNameOrEmail).trim();
            return new UserNotFoundException("User \"" + trimmedUserNameOrEmail + "\" not found.");
        });
    }

    /**
     * Gets a listing of all users given the offset, and count.  Additionally, the user requested must be active.  Users
     * not active will be treated as if they do not exist.
     *
     * @param offset the offset
     * @param count  the count
     * @return the users in the system
     */
    Pagination<User> getUsers(int offset, int count);

    /**
     * Gets a listing of all users given the offset, and count.  Additionally, the
     * user requested must be active.
     *
     * @param offset the offset
     * @param count  the count
     * @param query  a query to filter the results
     * @return the users in the system
     */
    Pagination<User> getUsers(int offset, int count, String query);

    /**
     * Gets a listing of all users given the offset, count and phone number
     *
     * @param offset the offset
     * @param count  the count
     * @param phone  a phone numberto filter the results
     * @return the users in the system
     */
    Pagination<User> getUsersByPrimaryPhoneNumbers(int offset, int count, List<String> phone);

    /**
     * Creates a user with the given User object.  Using "Strict" semantics, if the user exists
     * then this will throw an exception.  The resulting user will have a scrambled password.
     *
     * @param user the user to create
     * @return the User as it was created.
     */
    User createUserStrict(User user);

    /**
     * Creates a user with the given User object and password.  Using "Strict" semantics, if the
     * user exists then this will throw an exception.  The resulting user will be assigned
     * the given password.
     *
     * @param user the user to create
     * @param password the password to assign the user
     * @return the User as it was created.
     */
    User createUserWithPasswordStrict(User user, String password);

    /**
     * Creates or activates a user, or if the user is currently inactive
     * this will reinstate access.  This securely scrambles the user's password
     * and therefore the user must change password at a later date.
     * <p>
     * Similar to {@link #createUserStrict(User)} the user will be assigned a scrambled
     * password if the user does not exist (or was previously inactive). This will not
     * touch the user's password if the user both exists and was flagged as active.
     *
     * @param user the user
     * @return the User, as written to the database
     */
    User createUser(User user);

    /**
     * Creates a user and sets the user's password.  If the user exists
     * then this will reinstate the user's account with a new password.
     *
     * @param user     the user to create
     * @param password the password for the user to use
     * @return the User, as was written to the database
     */
    User createUserWithPassword(User user, String password);

    /**
     * Updates the given user, regardless of active status and then returns
     * the user instance as it was written to the database.
     * <p>
     * This will only apply to a {@link User} with a {@link UserUid} containing a schema with an id prepended with
     * dev.getelements. Other schemas will be assumed to external login methods.
     * </p>
     * This does not change the user's password.
     *
     * @param user the user to update
     * @return the user as was written to the database
     */
    User updateUserStrict(User user);

    /**
     * Updates the given user, regardless of active status and then returns
     * the user instance as it was written to the database.
     * <p>
     * This will only apply to a {@link User} with a {@link UserUid} containing a schema with an id prepended with
     * dev.getelements. Other schemas will be assumed to external login methods.
     * </p>
     * This will update the user's password.
     *
     * @param user the user to update
     * @return the user as was written to the database
     */
    User updateUserStrict(User user, String password);

    /**
     * Updates the given active user.  If the user has been deleted or has been
     * flagged as inactive, then this method will fail.
     * <p>
     * This will only apply to a {@link User} with a {@link UserUid} containing a schema with an id prepended with
     * dev.getelements. Other schemas will be assumed to external login methods.
     * </p>
     * This does not change the user's password.
     *
     * @param user the user to update
     * @return the User as written to the database
     */
    User updateUser(User user);

    /**
     * Updates the given active user.  If the user has been deleted or has been
     * flagged as inactive, then this method will fail.
     * <p>
     * This will only apply to a {@link User} with a {@link UserUid} containing a schema with an id prepended with
     * dev.getelements. Other schemas will be assumed to external login methods.
     * </p>
     * @param user     the user to update
     * @param password the user password
     * @return the User, as written to the database
     */
    User updateUser(User user, String password);

    /**
     * Updates the given active user.  If the user has been deleted or has been
     * flagged as inactive, then this method will fail.
     * <p>
     * This will only apply to a {@link User} with a {@link UserUid} containing a schema with an id prepended with
     * dev.getelements. Other schemas will be assumed to external login methods.
     * </p>
     * @param user        the user to update
     * @param newPassword the user's new password
     * @param oldPassword the user's old password
     * @return the User, as written to the database
     */
    User updateUser(User user, String newPassword, String oldPassword);


    /**
     * Creates a user if one does not exist for the provided info or updates a user if one does exist.
     * <p>
     * If the user is currently inactive this will reinstate access.  This securely scrambles the user's password
     * and therefore the user must change password at a later date.
     * <p>
     * Similar to {@link #createUserStrict(User)} the user will be assigned a scrambled
     * password if the user does not exist (or was previously inactive). This will not
     * touch the user's password if the user both exists and was flagged as active.
     *
     * @param user the user
     * @return the User, as written to the database
     */
    User createOrUpdateUser(User user);

    /**
     * Deletes a user from the database.  In actuality, this isn't a true delete, but
     * rather just flags the user as inactive.  LazyValue flagged inactive, a user will
     * not show up in any results for active users.
     *
     * @param userId the user's as determined by {@link User#getId()}
     */
    void softDeleteUser(String userId);

    /**
     * Validates the user's password and returns the current User instance.  If the password validation fails, then this
     * simply throws an instance of {@link ForbiddenException}
     *
     * @param userNameOrEmail the user's name or email address
     * @param password        the password
     * @return the User, never null
     */
    default User validateUserPassword(String userNameOrEmail, String password) {
        return findUserWithLoginAndPassword(userNameOrEmail, password).orElseThrow(ForbiddenException::new);
    }

    /**
     * Finds a {@link User} given the login credentials and assword.  If the password validation fails, then this will
     * return an empty {@link Optional}. If the password validation succeeds, then this returns the user that matched.
     *
     * @param userNameOrEmail the user's name or email address
     * @param password        the password
     * @return the User, never null
     */
    Optional<User> findUserWithLoginAndPassword(String userNameOrEmail, String password);

}

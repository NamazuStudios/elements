package dev.getelements.elements.dao;

import dev.getelements.elements.model.user.User;

/**
 * Performs DAO operations on a {@link User} from the context of the Google Sign-In process. THis supports linking of
 * Elements users with Google Sign-in credentials.
 */
public interface GoogleSignInUserDao {

    /**
     * Connects the supplied user to Google Sign-In, if not already done so.
     *
     * @param user the user
     * @return the User, as written to the database
     */
    User connectActiveUserIfNecessary(User user);

    /**
     * Creates, reactivates, or updates a user.  Unlike the operations in {@link UserDao}, this queries for user based
     * on Google Sign-In ID and will base all operations on that key.
     *
     * Similar to {@link UserDao#createUserStrict(User)} the user will be assigned a scrambled password if the user does
     * not exist or was previously inactive) This will not touch the user's password if the user both exists and was
     * flagged as active.
     *
     * As Google provides no good way to derive a unique name, this call will assign a random qualifier to the user name
     * provided to ensure the resulting user name is unique.
     *
     * @param user the user
     * @return the User, as written to the database
     */
    User createReactivateOrUpdateUser(final User user);

}

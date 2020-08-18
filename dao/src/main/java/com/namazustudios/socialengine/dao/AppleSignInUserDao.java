package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.user.User;

/**
 * Performs DAO operations on a {@link User} from the context of the Apple Sign-In process. THis supports linking of
 * Elements users with Apple Sign-in credentials.
 */
public interface AppleSignInUserDao {

    /**
     * Connects the supplied user to facebook, if not already done so.
     *
     * @param user the user
     * @return the User, as written to the database
     */
    User connectActiveAppleUserIfNecessary(User user);

    /**
     * Creates, reactivates, or updates a user.  Unlike the operations in {@link UserDao}, this
     * queries for user based on Facebook ID.
     *
     * Similar to {@link UserDao#createUserStrict(User)} the user will be assigned a scrambled
     * password if the user does not exist (or was previously inactive). This will not
     * touch the user's password if the user both exists and was flagged as active.
     *
     * @param user the user
     * @return the User, as written to the database
     */
    User createReactivateOrUpdateUser(final User user);

}

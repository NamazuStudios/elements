package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.User;

/**
 * Similar to the {@link UserDao} but contains {@link User} operations related to Facebook
 * operations.
 *
 * Created by patricktwohig on 6/25/17.
 */
public interface FacebookUserDao {



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

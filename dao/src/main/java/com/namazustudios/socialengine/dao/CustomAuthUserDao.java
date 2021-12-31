package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.auth.UserClaim;
import com.namazustudios.socialengine.model.auth.UserKey;
import com.namazustudios.socialengine.model.user.User;

import java.util.Optional;

/**
 * {@link User} database operations when dealing with custom auth schemes.
 */
public interface CustomAuthUserDao {

    /**
     * Upserts the user with the supplied {@link UserClaim}.
     *
     * @param userKey the key for determining the existing user.
     * @param subject the subject to use when looking up the user.
     * @param userClaim the user claim
     *
     * @return the {@link User} as was inserted in the database
     */
    User upsertUser(UserKey userKey, String subject, UserClaim userClaim);

    /**
     * Finds the user with the supplied {@link UserKey} and subject.
     *
     * @param userKey the userKey
     * @param subject the subject
     * @return an {@link Optional<User>}
     */
    Optional<User> findActiveUser(UserKey userKey, String subject);

    /**
     * Gets the user with the supplied {@link UserKey} and subject.
     *
     * @param subject the subject
     * @return the {@link User}, never null
     */
    default User getActiveUser(final UserKey userKey, String subject) {
        return findActiveUser(userKey, subject).orElseThrow(UserNotFoundException::new);
    }

}

package com.namazustudios.socialengine.security;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;

/**
 * Represents a supported authentication method.  This returns an instance of {@link User} if authenticaion is
 * successful, and throws and instance of {@link ForbiddenException} in theevent of a failure.
 *
 * Created by patricktwohig on 6/26/17.
 */
public interface UserAuthenticationMethod {

    /**
     * Attempts authorization.  If authorization fails, then this must throw an instance of {@link ForbiddenException}.
     * Subsequence methods may be used before finally failing authentication with a {@link ForbiddenException}.
     *
     * @return the {@link User}, never null
     */
    User attempt() throws ForbiddenException;

    /**
     * The final {@link UserAuthenticationMethod} which returns the result of
     * {@link User#getUnprivileged()}.  This is guaranteed not to throw an
     * exception.
     */
    UserAuthenticationMethod UNPRIVILEGED = () -> User.getUnprivileged();

}

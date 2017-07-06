package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;

/**
 * Represents a supported authentication method.  This returns an instance of {@link User} if
 * authenticaion is successful, and throws and instance of {@link ForbiddenException} in the
 * event of a failure.
 *
 * Created by patricktwohig on 6/26/17.
 */
public interface UserAuthenticationMethod {

    /**
     * Attempts authorization.  If authorization fails, then this must throw
     * and instance of {@link ForbiddenException}.
     *
     * @return the {@link User}
     */
    User attempt();

    /**
     * The final {@link UserAuthenticationMethod} which returns the result of
     * {@link User#getUnprivileged()}.  This is guaranteed not to throw an
     * exception.
     */
    UserAuthenticationMethod UNPRIVILEGED = () -> User.getUnprivileged();

}

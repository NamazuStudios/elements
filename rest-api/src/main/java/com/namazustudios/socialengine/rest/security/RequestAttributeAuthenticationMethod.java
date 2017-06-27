package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.User;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;

/**
 * Uses a property on the {@link ContainerRequestContext} to supply the {@link User}.
 *
 * Created by patricktwohig on 6/26/17.
 */
public class RequestAttributeAuthenticationMethod implements UserAuthenticationMethod {

    public static final String USER_REQUEST_ATTRIBUTE = User.class.getName();

    private HttpServletRequest httpServletRequest;

    @Override
    public User attempt() {

        final Object user = getHttpServletRequest().getAttribute(USER_REQUEST_ATTRIBUTE);

        if (user == null) {
            throw new ForbiddenException();
        } else if (!(user instanceof User)) {
            throw new InternalException("Unable to Authenticate");
        }

        return (User) user;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    @Inject
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

}

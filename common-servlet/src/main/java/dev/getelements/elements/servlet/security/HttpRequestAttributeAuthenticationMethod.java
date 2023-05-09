package dev.getelements.elements.servlet.security;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.security.UserAuthenticationMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import static dev.getelements.elements.model.user.User.USER_ATTRIBUTE;

/**
 * Uses a property on the {@link HttpServletRequest} to supply the {@link User}.  The auhenticated
 * user is set to the request using {@link HttpServletRequest#setAttribute(String, Object)} using
 * {@link User#USER_ATTRIBUTE} as the key.
 *
 * Created by patricktwohig on 6/26/17.
 */
public class HttpRequestAttributeAuthenticationMethod implements UserAuthenticationMethod {

    private HttpServletRequest httpServletRequest;

    @Override
    public User attempt() {

        final Object user = getHttpServletRequest().getAttribute(USER_ATTRIBUTE);

        if (user == null) {
            throw new ForbiddenException();
        } else if (!(user instanceof User)) {
            throw new InternalException();
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

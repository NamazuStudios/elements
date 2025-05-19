package dev.getelements.elements.servlet.security;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;

import static dev.getelements.elements.sdk.model.user.User.USER_ATTRIBUTE;

public class HttpRequestAttributeUserProvider implements Provider<User> {

    private HttpServletRequest httpServletRequest;

    @Override
    public User get() {

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

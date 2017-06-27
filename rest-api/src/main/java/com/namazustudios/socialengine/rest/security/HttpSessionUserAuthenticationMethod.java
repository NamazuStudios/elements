package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class HttpSessionUserAuthenticationMethod implements UserAuthenticationMethod {

    public static final String USER_SESSION_KEY = User.class.getName();

    private HttpServletRequest httpServletRequest;

    @Override
    public User attempt() {

        final HttpSession httpSession = getHttpServletRequest().getSession(false);

        if (httpSession == null) {
            throw new ForbiddenException();
        }

        final User user = (User) getHttpServletRequest()
            .getSession()
            .getAttribute(USER_SESSION_KEY);

        if (user == null) {
            throw new ForbiddenException();
        }

        return user;

    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    @Inject
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

}

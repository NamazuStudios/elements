package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.UserAuthenticationMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.namazustudios.socialengine.model.User.USER_ATTRIBUTE;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class HttpSessionUserAuthenticationMethod implements UserAuthenticationMethod {

    private HttpServletRequest httpServletRequest;

    @Override
    public User attempt() {

        final HttpSession httpSession = getHttpServletRequest().getSession(false);

        if (httpSession == null) {
            throw new ForbiddenException();
        }

        final User user = (User) getHttpServletRequest()
            .getSession()
            .getAttribute(USER_ATTRIBUTE);

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

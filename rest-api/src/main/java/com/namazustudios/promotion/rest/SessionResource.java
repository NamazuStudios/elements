package com.namazustudios.promotion.rest;

import com.google.common.base.Strings;
import com.namazustudios.promotion.exception.InvalidDataException;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.rest.provider.UserProvider;
import com.namazustudios.promotion.service.AuthService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Path("session")
public class SessionResource {

    @Inject
    private AuthService authService;

    @Inject
    private HttpServletRequest httpServletRequest;

    @GET
    public User getSession(@QueryParam("userId") String userId,
                           @QueryParam("password") String password) {

        userId = Strings.nullToEmpty(userId).trim();
        password = Strings.nullToEmpty(password).trim();

        if (Strings.isNullOrEmpty(userId)) {
            throw new InvalidDataException("User ID must be specified.");
        }

        if (Strings.isNullOrEmpty(password)) {
            throw new InvalidDataException("Password must be specified.");
        }

        final User user = authService.loginUser(userId, password);
        final HttpSession httpSession = httpServletRequest.getSession(true);

        httpSession.setAttribute(UserProvider.USER_SESSION_KEY, user);

        return user;

    }

    @DELETE
    public void destroySession() {
        httpServletRequest.getSession().invalidate();
    }

}

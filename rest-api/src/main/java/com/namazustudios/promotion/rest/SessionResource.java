package com.namazustudios.promotion.rest;

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
    public User getSession(@PathParam("userId") final String userId, @PathParam("password") final String password) {

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

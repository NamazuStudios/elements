package com.namazustudios.socialengine.rest;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.rest.provider.UserProvider;
import com.namazustudios.socialengine.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Api(value = "Session and Login",
     description = "Starts a session by associating a User with the current HTTP session.")
@Path("session")
public class SessionResource {

    @Inject
    private AuthService authService;

    @Inject
    private HttpServletRequest httpServletRequest;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a Session",
                  notes = "Begins a session by accepting both the UserID and the Passoword.  Upon successful " +
                          "completion of this call a cookie is set which can be used to auth future requests.  If " +
                          "either the cookie or the underlying session expires, the user will have to reestablish " +
                          "the session by supplying credentials again.")
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
    @ApiOperation(value = "Destroys the Session",
                  notes = "Simply invalidates the session and effectively logs the user out.")
    public void destroySession() {
        httpServletRequest.getSession().invalidate();
    }

}

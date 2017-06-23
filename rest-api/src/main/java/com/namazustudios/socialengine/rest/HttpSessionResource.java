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
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Api(value = "Session and Login",
     description = "Starts a session by associating a User with the current HTTP session.")
@Path("session/http")
public class HttpSessionResource {

    @Inject
    private AuthService authService;

    @Inject
    private HttpServletRequest httpServletRequest;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a Session",
                  notes = "Begins a session by accepting both the UserID and the Passoword.  Upon successful " +
                          "completion of this call, the user will be added to the current HTTP session.  If " +
                          "the session expires, the user will have to reestablish the session by supplying " +
                          "credentials again.  This is most useful for applications delivered in a web page.")
    public User createSession(@QueryParam("userId") String userId,
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

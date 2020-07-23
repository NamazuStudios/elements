package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.SessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Api(value = "Session and Login",
     description = "Creates a Session instance from a username and password.")
@Path("session")
public class SessionResource {

    private User user;

    private SessionService sessionService;

    @DELETE
    @ApiOperation(value = "Destroys the Session")
    public void destroySessions() {
        getSessionService().destroySessions(user.getId());
    }

    @DELETE
    @ApiOperation(value = "Destroys the Session")
    @Path("{sessionSecret}")
    public void destroySession(@PathParam("sessionSecret") final String sessionSecret) {
        getSessionService().destroySession(user.getId(), sessionSecret);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Inject
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

}

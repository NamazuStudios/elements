package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.model.session.Session;
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

    private SessionService sessionService;

    @DELETE
    @ApiOperation(value = "Destroys the Session")
    public void destroySessions() {
        getSessionService().destroySessions();
    }

    @DELETE
    @ApiOperation(value = "Destroys the Session")
    @Path("{sessionId}")
    public void destroySession(@PathParam("sessionId") final String sessionId) {
        getSessionService().destroySession(sessionId);
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Inject
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

}

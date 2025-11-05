package dev.getelements.elements.rest.security;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.auth.SessionService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("session")
public class SessionResource {

    private SessionService sessionService;

    @DELETE
    @Operation(summary = "Destroys the Session")
    @Path("{sessionSecret}")
    public void blacklistSession(@PathParam("sessionSecret") final String sessionSecret) {
        getSessionService().blacklistSession(sessionSecret);
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Inject
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

}

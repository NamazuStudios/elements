package dev.getelements.elements.rest.security;

import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.UsernamePasswordAuthService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Path("session")
public class UsernamePasswordResource {

    private UsernamePasswordAuthService usernamePasswordAuthService;

    private HttpServletRequest httpServletRequest;

    private ValidationHelper validationHelper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a Session",
        description = "Begins a session by accepting both the UserID and the Password.  Upon successful " +
                          "completion of this call, the user will be added to the current HTTP session.  If " +
                          "the session expires, the user will have to reestablish the session by supplying " +
                          "credentials again.  This is most useful for applications delivered in a web page.")
    public SessionCreation createUsernamePasswordSession(final UsernamePasswordSessionRequest usernamePasswordSessionRequest) {
        getValidationHelper().validateModel(usernamePasswordSessionRequest);
        return getUsernamePasswordAuthService().createSession(usernamePasswordSessionRequest);
    }

    public UsernamePasswordAuthService getUsernamePasswordAuthService() {
        return usernamePasswordAuthService;
    }

    @Inject
    public void setUsernamePasswordAuthService(UsernamePasswordAuthService usernamePasswordAuthService) {
        this.usernamePasswordAuthService = usernamePasswordAuthService;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    @Inject
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}

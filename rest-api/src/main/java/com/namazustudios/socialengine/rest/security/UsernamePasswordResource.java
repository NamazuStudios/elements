package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.session.UsernamePasswordSessionRequest;
import com.namazustudios.socialengine.service.UsernamePasswordAuthService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Api(value = "UsernamePasswordSession",
     description = "Creates a Session instance from a username and password.")
@Path("session")
public class UsernamePasswordResource {

    private UsernamePasswordAuthService usernamePasswordAuthService;

    private HttpServletRequest httpServletRequest;

    private ValidationHelper validationHelper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a Session",
                  notes = "Begins a session by accepting both the UserID and the Passoword.  Upon successful " +
                          "completion of this call, the user will be added to the current HTTP session.  If " +
                          "the session expires, the user will have to reestablish the session by supplying " +
                          "credentials again.  This is most useful for applications delivered in a web page.")
    public SessionCreation createSession(final UsernamePasswordSessionRequest usernamePasswordSessionRequest) {

        getValidationHelper().validateModel(usernamePasswordSessionRequest);

        final String userId = usernamePasswordSessionRequest.getUserId().trim();
        final String password = usernamePasswordSessionRequest.getPassword().trim();
        final String profileId = nullToEmpty(usernamePasswordSessionRequest.getProfileId()).trim();

        if (isNullOrEmpty(userId)) {
            throw new InvalidDataException("User ID must be specified.");
        }

        if (isNullOrEmpty(password)) {
            throw new InvalidDataException("Password must be specified.");
        }

        return profileId.isEmpty() ?
            getUsernamePasswordAuthService().createSessionWithLogin(userId, password) :
            getUsernamePasswordAuthService().createSessionWithLogin(userId, password, profileId);

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

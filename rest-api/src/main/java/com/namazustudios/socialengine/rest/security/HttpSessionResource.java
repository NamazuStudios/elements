package com.namazustudios.socialengine.rest.security;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.UsernamePasswordSessionRequest;
import com.namazustudios.socialengine.service.AuthService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.print.attribute.standard.Media;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.model.User.USER_ATTRIBUTE;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Api(value = "Session and Login",
     description = "Starts a session by associating a User with the current HTTP session.")
@Path("session/http")
public class HttpSessionResource {

    private AuthService authService;

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
    public Session createSession(final UsernamePasswordSessionRequest usernamePasswordSessionRequest) {

        getValidationHelper().validateModel(usernamePasswordSessionRequest);

        final String userId = usernamePasswordSessionRequest.getUserId().trim();
        final String password = usernamePasswordSessionRequest.getPassword().trim();

        if (isNullOrEmpty(userId)) {
            throw new InvalidDataException("User ID must be specified.");
        }

        if (isNullOrEmpty(password)) {
            throw new InvalidDataException("Password must be specified.");
        }

        return getAuthService().createSessionWithLogin(userId, password);

    }

    @DELETE
    @ApiOperation(value = "Destroys the Session",
                  notes = "Simply invalidates the session and effectively logs the user out.")
    public void destroySession() {
        httpServletRequest.getSession().invalidate();
    }

    public AuthService getAuthService() {
        return authService;
    }

    @Inject
    public void setAuthService(AuthService authService) {
        this.authService = authService;
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

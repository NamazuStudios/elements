package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.FacebookSession;
import com.namazustudios.socialengine.service.FacebookAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.model.User.*;

/**
 * Created by patricktwohig on 6/22/17.
 */
@Path("application/{applicationNameOrId}/session/facebook/{applicationConfigurationNameOrId}")
@Api(value = "Facebook Session and Login",
     description = "Allows the user to login and create accoutns using Facebook.")

public class FacebookSessionResource {

    private HttpServletRequest httpServletRequest;

    private FacebookAuthService facebookAuthService;

    @POST
    @ApiOperation(value = "Gets a Session using Facebook",
            notes = "Begins a session by accepting the facebook token for the particular application.  Unlike " +
                    "sessions backed by HTTP sessions, Facebook sessions must be created under the " +
                    "application.  This may implicitly create a new User account.  Additionally, the returned " +
                    "token is scoped to the application which created it.  This may have effects on how subsequent " +
                    "requests will behave based on the requested token.  Subsequent requests require that the supplied " +
                    "long-term Facebook token be supplied in authorization headers.")
    @Produces(MediaType.APPLICATION_JSON)
    public FacebookSession createSession(

            @ApiParam("The application name or id")
            @PathParam("applicationNameOrId")
            final String applicationNameOrId,

            @ApiParam("The application configuation name or id.  For Facebook applications this is the Facebook App ID.")
            @PathParam("applicationConfigurationNameOrId")
            final String applicationConfigurationNameOrId,

            @QueryParam("facebookOAuthAccessToken")
            @ApiParam(value = "The Facebook OAuth token (should be a short-lived token).", required = true)
            String facebookOAuthAccessToken) {

        facebookOAuthAccessToken = nullToEmpty(facebookOAuthAccessToken).trim();

        if (isNullOrEmpty(facebookOAuthAccessToken)) {
            throw new InvalidDataException("Must specify OAuth Token.");
        }

        final FacebookSession facebookSession;
        facebookSession = getFacebookAuthService()
            .createOrUpdateUserWithFacebookOAuthAccessToken(applicationNameOrId, applicationConfigurationNameOrId, facebookOAuthAccessToken);

        final User user = facebookSession.getUser();
        final HttpSession httpSession = getHttpServletRequest().getSession(true);
        httpSession.setAttribute(USER_ATTRIBUTE, user);

        return facebookSession;

    }

    public FacebookAuthService getFacebookAuthService() {
        return facebookAuthService;
    }

    @Inject
    public void setFacebookAuthService(FacebookAuthService facebookAuthService) {
        this.facebookAuthService = facebookAuthService;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    @Inject
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

}

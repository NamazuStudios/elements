package com.namazustudios.socialengine.rest.application;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.FacebookSession;
import com.namazustudios.socialengine.service.FacebookAuthService;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.provider.UserProvider.USER_SESSION_KEY;

/**
 * Created by patricktwohig on 6/22/17.
 */
@Path("application/{applicationNameOrId}/session/facebook")
public class FacebookSessionResource {

    private HttpServletRequest httpServletRequest;

    private FacebookAuthService facebookAuthService;

    @POST
    @ApiOperation(value = "Gets a Session using Facebook",
            notes = "Begins a session by accepting the facebook token for the particular application.  Unlike " +
                    "sessions backed by HTTP sessions, Facebook sessions must be created under the " +
                    "application.  This may implicitly create a new User account.  Additionally, the returned " +
                    "token is scoped to the application which created it.  This may have effects on how subsequent " +
                    "requests will behave based on the requested token.")
    public FacebookSession createSession(
        @PathParam("applicationNameOrId") final String applicationNameOrId,
        @QueryParam("facebookToken")      String facebookToken) {

        facebookToken = nullToEmpty(facebookToken).trim();

        if (Strings.isNullOrEmpty(applicationNameOrId)) {
            throw new InvalidDataException("Must specify OAuth Token.");
        }

        final FacebookSession facebookSession;
        facebookSession = getFacebookAuthService().authorizeWithToken(applicationNameOrId, facebookToken);

        final User user = facebookSession.getUser();
        final HttpSession httpSession = httpServletRequest.getSession(true);
        httpSession.setAttribute(USER_SESSION_KEY, user);

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

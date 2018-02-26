package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.session.FacebookSession;
import com.namazustudios.socialengine.model.session.FacebookSessionRequest;
import com.namazustudios.socialengine.service.FacebookAuthService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Api(value = "Session and Login",
        description = "Creates a Session instance from a username and password.")
@Path("session_facebook")
public class FacebookAuthResource {

    private ValidationHelper validationHelper;

    private FacebookAuthService facebookAuthService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a Session using Facebook",
            notes = "Begins a session by accepting a Facebook OAuth token, SocialEngine Application ID, and the " +
                    "configuration ID for the application.  This will generate a Session instance and return the " +
                    "result to the client.")
    public FacebookSession createSession(final FacebookSessionRequest facebookSessionRequest) {

        getValidationHelper().validateModel(facebookSessionRequest);

        final String applicationNameOrId = facebookSessionRequest.getApplicationNameOrId();
        final String applicationConfigurationNameOrId = facebookSessionRequest.getApplicationConfigurationNameOrId();
        final String facebookOAuthAccessToken = facebookSessionRequest.getFacebookOAuthAccessToken();

        if (isNullOrEmpty(applicationNameOrId)) {
            throw new InvalidDataException("Application Name not Specified");
        }

        if (isNullOrEmpty(applicationConfigurationNameOrId)) {
            throw new InvalidDataException("Application Configuration not Specified");
        }

        if (isNullOrEmpty(facebookOAuthAccessToken)) {
            throw new InvalidDataException("Facebook OAuth Token not specified.");
        }

        return getFacebookAuthService().createOrUpdateUserWithFacebookOAuthAccessToken(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                facebookOAuthAccessToken);

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public FacebookAuthService getFacebookAuthService() {
        return facebookAuthService;
    }

    @Inject
    public void setFacebookAuthService(FacebookAuthService facebookAuthService) {
        this.facebookAuthService = facebookAuthService;
    }

}

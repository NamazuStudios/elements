package dev.getelements.elements.rest.security;

import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.model.session.GoogleSignInSessionCreation;
import dev.getelements.elements.model.session.GoogleSignInSessionRequest;
import dev.getelements.elements.service.GoogleSignInAuthService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;
import static dev.getelements.elements.rest.AuthSchemes.*;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Api(value = "GoogleSignInSession",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("googlesignin_session")
public class GoogleSignInAuthResource {

    private ValidationHelper validationHelper;

    private GoogleSignInAuthService googleSignInAuthService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a Session using Google Sign-In",
            notes = "Begins a session by accepting an auth code and identity token. Upon successful validation against " +
                    "Google's sign-in APIs, this will return a Session which can be used for authentication. If there is" +
                    "no User associated with the supplied credentials, this will implicitly create a new account and " +
                    "will include that account information in the response. If there is an account, or this method " +
                    "receives an existing session key, this will link the existing account to google if the account was " +
                    "not previously linked.")
    public GoogleSignInSessionCreation createGoogleSession(final GoogleSignInSessionRequest googleSignInSessionRequest) {

        getValidationHelper().validateModel(googleSignInSessionRequest);

        final String applicationNameOrId = googleSignInSessionRequest.getApplicationNameOrId();
        final String identityToken = googleSignInSessionRequest.getIdentityToken();

        if (isNullOrEmpty(applicationNameOrId)) {
            throw new InvalidDataException("Application Name not Specified");
        }

        if (isNullOrEmpty(identityToken)) {
            throw new InvalidDataException("Identity token not specified.");
        }

        return getGoogleSignInAuthService().createOrUpdateUserWithIdentityToken(applicationNameOrId, identityToken);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public GoogleSignInAuthService getGoogleSignInAuthService() {
        return googleSignInAuthService;
    }

    @Inject
    public void setGoogleSignInAuthService(GoogleSignInAuthService googleSignInAuthService) {
        this.googleSignInAuthService = googleSignInAuthService;
    }

}

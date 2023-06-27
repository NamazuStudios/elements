package dev.getelements.elements.rest.security;

import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.model.session.AppleSignInSessionCreation;
import dev.getelements.elements.model.session.AppleSignInSessionRequest;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.service.AppleSignInAuthService;
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
import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by patricktwohig on 4/2/15.
 */
@Api(value = "AppleSignInSession",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("applesignin_session")
public class AppleSignInAuthResource {

    private ValidationHelper validationHelper;

    private AppleSignInAuthService appleSignInAuthService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Creates a Session using Apple Sign-In",
        notes = "Begins a session by accepting an auth code and identity token. Upon successful validation against " +
                "Apple's sign-in APIs, this will return a Session which can be used for authentication. If there is" +
                "no User associated with the supplied credentials, this will implicitly create a new account and " +
                "will include that account information in the response. If there is an account, or this method " +
                "receives an existing session key, this will link the existing account to apple if the account was " +
                "not previously linked.")
    public AppleSignInSessionCreation createAppleSession(final AppleSignInSessionRequest appleSignInSessionRequest) {

        getValidationHelper().validateModel(appleSignInSessionRequest);

        final String applicationNameOrId = appleSignInSessionRequest.getApplicationNameOrId();
        final String applicationConfigurationNameOrId = appleSignInSessionRequest.getApplicationConfigurationNameOrId();

        final String authCode = appleSignInSessionRequest.getAuthCode();
        final String identityToken = appleSignInSessionRequest.getIdentityToken();

        if (isNullOrEmpty(applicationNameOrId)) {
            throw new InvalidDataException("Application Name not Specified");
        }

        if (isNullOrEmpty(applicationConfigurationNameOrId)) {
            throw new InvalidDataException("Application Configuration not Specified");
        }

        if (isNullOrEmpty(authCode)) {
            throw new InvalidDataException("Auth code not specified.");
        }

        if (isNullOrEmpty(identityToken)) {
            throw new InvalidDataException("Identity token not specified.");
        }

        return getAppleSignInAuthService().createOrUpdateUserWithIdentityTokenAndAuthCode(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                identityToken,
                authCode);

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public AppleSignInAuthService getAppleSignInAuthService() {
        return appleSignInAuthService;
    }

    @Inject
    public void setAppleSignInAuthService(AppleSignInAuthService appleSignInAuthService) {
        this.appleSignInAuthService = appleSignInAuthService;
    }

}

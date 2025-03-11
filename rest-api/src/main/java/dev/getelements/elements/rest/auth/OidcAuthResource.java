package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;

@Path("auth/oidc")
public class OidcAuthResource {

    private ValidationHelper validationHelper;

    private OidcAuthService oidcAuthService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a Session using OIDC",
            description = "Begins a session by accepting a JWT. Upon successful validation against " +
                    "the scheme provided in the path, this will return a Session which can be used for authentication. " +
                    "If there is no User associated with the supplied credentials, this will implicitly create a new account and " +
                    "will include that account information in the response. If there is an account, or this method " +
                    "receives an existing session key, this will link to the existing scheme if the account was " +
                    "not previously linked.")
    public SessionCreation createOidcSession(final OidcSessionRequest oidcSessionRequest) {

        getValidationHelper().validateModel(oidcSessionRequest);

        final String identityToken = oidcSessionRequest.getJwt();

        if (isNullOrEmpty(identityToken)) {
            throw new InvalidDataException("JWT is missing from the request.");
        }

        return getOidcAuthService().createSession(oidcSessionRequest);

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public OidcAuthService getOidcAuthService() {
        return oidcAuthService;
    }

    @Inject
    public void setOidcAuthService(OidcAuthService oidcAuthService) {
        this.oidcAuthService = oidcAuthService;
    }

}

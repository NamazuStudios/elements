package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Direct, synchronous OIDC login: the client already possesses an {@code id_token} (e.g. from a native platform
 * Sign-In SDK) and validates it against a statically-configured OIDC Auth Scheme in one call. For a client that
 * has no other way to obtain an {@code id_token}, see {@link OidcSessionResource}'s browser-redirect flow instead;
 * that resource's {@code idToken} shortcut shares this validation code path.
 */
@Path("auth/oidc")
public class OidcAuthResource {

    private ValidationHelper validationHelper;

    private OidcAuthService oidcAuthService;

    /**
     * Validates the supplied {@code id_token} and returns the resulting session, implicitly creating a new
     * account if no user is associated with the supplied credentials yet, or linking to the scheme if the
     * caller's existing session/account was not previously linked to it.
     *
     * @param oidcSessionRequest the JWT to validate
     * @return the resulting session
     */
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The JWT was valid; the resulting session is returned."),
            @ApiResponse(responseCode = "400", description = "The request failed validation, or is missing the JWT.")
    })
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

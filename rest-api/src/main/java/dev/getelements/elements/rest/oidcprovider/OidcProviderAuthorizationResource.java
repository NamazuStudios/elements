package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.exception.auth.OAuth2Exception;
import dev.getelements.elements.sdk.model.oidc.OidcProviderAuthorizationRequest;
import dev.getelements.elements.sdk.service.auth.OidcProviderAuthorizationService;
import dev.getelements.elements.sdk.service.Constants;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

/**
 * The provider's authorization endpoint. Validates the authorize request's parameters and redirects the
 * browser to the hosted login page, which carries the pending authorization's opaque id.
 */
@Path("oidc/authorize")
public class OidcProviderAuthorizationResource {

    private OidcProviderAuthorizationService oidcProviderAuthorizationService;

    @Inject
    @Named(Constants.OIDC_PROVIDER_ISSUER)
    private String issuer;

    @GET
    @Operation(
            summary = "Begins the OIDC authorization code flow.",
            description = "Validates the authorization request against the client's registration and redirects " +
                    "to the hosted login page. PKCE (S256) is required for all clients.")
    public Response authorize(@QueryParam("response_type") String responseType,
                              @QueryParam("client_id") String clientId,
                              @QueryParam("redirect_uri") String redirectUri,
                              @QueryParam("scope") String scope,
                              @QueryParam("state") String state,
                              @QueryParam("nonce") String nonce,
                              @QueryParam("code_challenge") String codeChallenge,
                              @QueryParam("code_challenge_method") String codeChallengeMethod) {

        final var request = new OidcProviderAuthorizationRequest();
        request.setResponseType(responseType);
        request.setClientId(clientId);
        request.setRedirectUri(redirectUri);
        request.setScope(scope);
        request.setState(state);
        request.setNonce(nonce);
        request.setCodeChallenge(codeChallenge);
        request.setCodeChallengeMethod(codeChallengeMethod);

        try {

            final OidcAuthorizationCode code = getOidcProviderAuthorizationService().beginAuthorization(request);

            final var loginUri = UriBuilder.fromUri(getIssuer() + "/oidc/login")
                    .queryParam("authorizationId", code.getCode())
                    .build();

            return Response.seeOther(loginUri).build();

        } catch (final OAuth2Exception ex) {

            // Per RFC 6749 section 4.1.2.1, errors raised after the client and redirect URI have been
            // validated are sent back to the relying party as query parameters; errors raised before are
            // not redirect-able and fall through to the exception mapper's OAuth error body.

            if (ex.getRedirectUri() == null) {
                throw ex;
            }

            final var errorRedirectUri = UriBuilder.fromUri(ex.getRedirectUri())
                    .queryParam("error", ex.getError())
                    .queryParam("error_description", ex.getErrorDescription());

            if (ex.getState() != null) {
                errorRedirectUri.queryParam("state", ex.getState());
            }

            return Response.seeOther(errorRedirectUri.build()).build();

        }

    }

    public OidcProviderAuthorizationService getOidcProviderAuthorizationService() {
        return oidcProviderAuthorizationService;
    }

    @Inject
    public void setOidcProviderAuthorizationService(OidcProviderAuthorizationService oidcProviderAuthorizationService) {
        this.oidcProviderAuthorizationService = oidcProviderAuthorizationService;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(@Named(Constants.OIDC_PROVIDER_ISSUER) String issuer) {
        this.issuer = issuer;
    }

}

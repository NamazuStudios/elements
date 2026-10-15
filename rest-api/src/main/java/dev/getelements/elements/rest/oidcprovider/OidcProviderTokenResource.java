package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.model.oidc.OidcProviderTokenRequest;
import dev.getelements.elements.sdk.model.oidc.OidcProviderTokenResponse;
import dev.getelements.elements.sdk.service.auth.OidcProviderTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Path("oidc/token")
public class OidcProviderTokenResource {

    private OidcProviderTokenService oidcProviderTokenService;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Exchanges an authorization code or refresh token for tokens.",
            description = "Redeems a single-use authorization code (grant_type=authorization_code, with PKCE " +
                    "S256 verification) or rotates a refresh token (grant_type=refresh_token). Returns the " +
                    "signed id_token, an Elements session token as the access_token, and a refresh token when " +
                    "the client's registration allows it. Client credentials may arrive in the form or via " +
                    "HTTP basic authentication.")
    @ApiResponse(responseCode = "200", description = "Tokens issued.",
            content = @Content(schema = @Schema(implementation = OidcProviderTokenResponse.class)))
    public OidcProviderTokenResponse token(final MultivaluedMap<String, String> form,
                                           @HeaderParam("Authorization") final String authorization) {

        final var request = new OidcProviderTokenRequest();
        request.setGrantType(form.getFirst("grant_type"));
        request.setCode(form.getFirst("code"));
        request.setRedirectUri(form.getFirst("redirect_uri"));
        request.setCodeVerifier(form.getFirst("code_verifier"));
        request.setRefreshToken(form.getFirst("refresh_token"));
        request.setClientId(form.getFirst("client_id"));
        request.setClientSecret(form.getFirst("client_secret"));

        final var basicCredentials = parseBasicCredentials(authorization);
        if (basicCredentials != null) {
            // Basic auth wins over form-supplied credentials; form fallback still applies when absent.
            if (request.getClientId() == null) {
                request.setClientId(basicCredentials[0]);
            }
            if (request.getClientSecret() == null) {
                request.setClientSecret(basicCredentials[1]);
            }
        }

        final var grantType = request.getGrantType();

        if ("authorization_code".equals(grantType)) {
            return getOidcProviderTokenService().exchangeCode(request);
        }

        if ("refresh_token".equals(grantType)) {
            return getOidcProviderTokenService().refresh(request);
        }

        throw new BadRequestException("Unsupported grant_type.");

    }

    private String[] parseBasicCredentials(final String authorization) {

        if (authorization == null || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            return null;
        }

        try {
            final var decoded = new String(
                    Base64.getDecoder().decode(authorization.substring(6).trim()), StandardCharsets.UTF_8);
            final var colon = decoded.indexOf(':');
            if (colon < 0) {
                return null;
            }
            return new String[]{decoded.substring(0, colon), decoded.substring(colon + 1)};
        } catch (IllegalArgumentException ex) {
            return null;
        }

    }

    public OidcProviderTokenService getOidcProviderTokenService() {
        return oidcProviderTokenService;
    }

    @Inject
    public void setOidcProviderTokenService(OidcProviderTokenService oidcProviderTokenService) {
        this.oidcProviderTokenService = oidcProviderTokenService;
    }

}

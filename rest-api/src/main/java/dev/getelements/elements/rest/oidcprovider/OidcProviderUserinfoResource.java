package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.model.oidc.OidcProviderUserInfo;
import dev.getelements.elements.sdk.service.auth.OidcProviderUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("oidc/userinfo")
public class OidcProviderUserinfoResource {

    private OidcProviderUserService oidcProviderUserService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Returns the claims for the presented access token's subject.",
            description = "Accepts an Elements session token (the access_token issued by this provider) as a " +
                    "Bearer credential. Claims are gated by the scopes recorded on the underlying grant: sub " +
                    "always; name and preferred_username under profile; email and email_verified under email.")
    @ApiResponse(responseCode = "200", description = "The userinfo claims.",
            content = @Content(schema = @Schema(implementation = OidcProviderUserInfo.class)))
    public OidcProviderUserInfo getUserInfo(@HeaderParam("Authorization") final String authorization) {
        return getOidcProviderUserService().getUserInfo(parseBearerToken(authorization));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Identical to the GET variant of the userinfo endpoint.")
    public OidcProviderUserInfo postUserInfo(@HeaderParam("Authorization") final String authorization) {
        return getOidcProviderUserService().getUserInfo(parseBearerToken(authorization));
    }

    private String parseBearerToken(final String authorization) {

        if (authorization == null || authorization.isBlank()) {
            throw new jakarta.ws.rs.NotAuthorizedException("Bearer");
        }

        if (authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7).trim();
        }

        throw new jakarta.ws.rs.NotAuthorizedException("Bearer");

    }

    public OidcProviderUserService getOidcProviderUserService() {
        return oidcProviderUserService;
    }

    @Inject
    public void setOidcProviderUserService(OidcProviderUserService oidcProviderUserService) {
        this.oidcProviderUserService = oidcProviderUserService;
    }

}

package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.model.auth.JWKSet;
import dev.getelements.elements.sdk.service.auth.OidcProviderJwksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(".well-known/jwks.json")
public class OidcProviderJwksResource {

    private OidcProviderJwksService oidcProviderJwksService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Returns the JWKS publishing this deployment's signing keys.",
            description = "Serves the public halves of the server's session-token signing keys as a JWK Set, " +
                    "including keys retired but still within their retention window, so tokens issued before " +
                    "a rotation continue to verify.")
    @ApiResponse(responseCode = "200", description = "The JWK Set.",
            content = @Content(schema = @Schema(implementation = JWKSet.class)))
    public JWKSet getJwks() {
        return getOidcProviderJwksService().getJwks();
    }

    public OidcProviderJwksService getOidcProviderJwksService() {
        return oidcProviderJwksService;
    }

    @Inject
    public void setOidcProviderJwksService(OidcProviderJwksService oidcProviderJwksService) {
        this.oidcProviderJwksService = oidcProviderJwksService;
    }

}

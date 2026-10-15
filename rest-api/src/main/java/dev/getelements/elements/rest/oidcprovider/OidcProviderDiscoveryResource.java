package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.model.oidc.OidcProviderDiscoveryDocument;
import dev.getelements.elements.sdk.service.auth.OidcProviderDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(".well-known/openid-configuration")
public class OidcProviderDiscoveryResource {

    private OidcProviderDiscoveryService oidcProviderDiscoveryService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Returns the OIDC provider discovery document.",
            description = "Serves the OIDC Discovery JSON document describing this deployment as an identity " +
                    "provider. The issuer is configuration-driven; the discovery document lives at " +
                    "<issuer>/.well-known/openid-configuration as the specification requires.")
    @ApiResponse(responseCode = "200", description = "The discovery document.",
            content = @Content(schema = @Schema(implementation = OidcProviderDiscoveryDocument.class)))
    public OidcProviderDiscoveryDocument getDiscoveryDocument() {
        return getOidcProviderDiscoveryService().getDiscoveryDocument();
    }

    public OidcProviderDiscoveryService getOidcProviderDiscoveryService() {
        return oidcProviderDiscoveryService;
    }

    @Inject
    public void setOidcProviderDiscoveryService(OidcProviderDiscoveryService oidcProviderDiscoveryService) {
        this.oidcProviderDiscoveryService = oidcProviderDiscoveryService;
    }

}

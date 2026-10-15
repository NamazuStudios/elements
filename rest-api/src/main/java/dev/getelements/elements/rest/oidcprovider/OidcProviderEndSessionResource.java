package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.service.auth.OidcProviderTokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("oidc/end-session")
public class OidcProviderEndSessionResource {

    private OidcProviderTokenService oidcProviderTokenService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(
            summary = "Ends the session identified by the supplied id_token_hint.",
            description = "Verifies the id_token_hint's signature, then revokes every grant the identified " +
                    "user holds with the identified client — backing Elements sessions, grants, and refresh " +
                    "tokens all die, so tokens already issued to the downstream application stop verifying.")
    public Response endSession(@QueryParam("id_token_hint") final String idTokenHint) {
        return doEndSession(idTokenHint);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Identical to the GET variant of the end-session endpoint.")
    public Response postEndSession(@FormParam("id_token_hint") final String idTokenHint) {
        return doEndSession(idTokenHint);
    }

    private Response doEndSession(final String idTokenHint) {

        getOidcProviderTokenService().endSession(idTokenHint);

        return Response.ok(
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>Signed out</title></head>\n" +
                "<body><h1>Signed out</h1><p>Your session with this application has ended.</p></body>\n" +
                "</html>\n",
                MediaType.TEXT_HTML)
                .build();

    }

    public OidcProviderTokenService getOidcProviderTokenService() {
        return oidcProviderTokenService;
    }

    @Inject
    public void setOidcProviderTokenService(OidcProviderTokenService oidcProviderTokenService) {
        this.oidcProviderTokenService = oidcProviderTokenService;
    }

}

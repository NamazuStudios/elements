package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.service.auth.OidcLoginAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * The provider-facing redirect target for the browser-redirect OIDC login flow. Registered as each provider's
 * redirect_uri; never called directly by a game client. Renders a minimal human-readable page and always returns
 * 200 with an HTML body — the outcome is communicated to the waiting client via {@link OidcSessionResource}'s
 * poll endpoint, not via this response.
 */
@Path("oidc/{provider}/callback")
public class OidcCallbackResource {

    private static final String SUCCESS_HTML =
            "<html><body><h1>Success</h1><p>You may now return to the game.</p></body></html>";

    private static final String FAILURE_HTML =
            "<html><body><h1>Login failed</h1><p>Please return to the game and try again.</p></body></html>";

    private OidcLoginAttemptService oidcLoginAttemptService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(
            summary = "OIDC provider redirect target",
            description = "Not called by game clients. The provider redirects the system browser here after " +
                    "the user completes (or denies) authorization.")
    public Response callback(@PathParam("provider") final String provider,
                              @QueryParam("code") final String code,
                              @QueryParam("state") final String state,
                              @QueryParam("error") final String error) {

        if (state == null || (error == null && code == null)) {
            return html(FAILURE_HTML);
        }

        try {
            getOidcLoginAttemptService().handleCallback(provider, code, state, error);
            return html(SUCCESS_HTML);
        } catch (final Exception ex) {
            // The exception detail is never surfaced in the page — it's recorded server-side via the attempt's
            // failure reason, retrievable through the poll endpoint.
            return html(FAILURE_HTML);
        }

    }

    private Response html(final String body) {
        return Response.ok(body, MediaType.TEXT_HTML).build();
    }

    public OidcLoginAttemptService getOidcLoginAttemptService() {
        return oidcLoginAttemptService;
    }

    @Inject
    public void setOidcLoginAttemptService(OidcLoginAttemptService oidcLoginAttemptService) {
        this.oidcLoginAttemptService = oidcLoginAttemptService;
    }

}

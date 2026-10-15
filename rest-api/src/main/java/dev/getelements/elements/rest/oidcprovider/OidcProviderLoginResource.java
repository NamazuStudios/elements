package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.model.oidc.OidcProviderLoginRequest;
import dev.getelements.elements.sdk.service.auth.OidcProviderAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

/**
 * The provider's hosted login page. A minimal, server-rendered form (135a; branding/theming is 135b): the
 * page carries the pending authorization's opaque id in a hidden field, and the POST authenticates the
 * submitted credentials and redirects the browser to the client's redirect URI with the code and state.
 */
@Path("oidc/login")
public class OidcProviderLoginResource {

    private OidcProviderAuthorizationService oidcProviderAuthorizationService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(
            summary = "Serves the hosted login page for a pending authorization.",
            description = "Renders a minimal credential form bound to the pending authorization. Submitting " +
                    "the form completes the authorization and redirects to the client.")
    public String getLoginPage(@QueryParam("authorizationId") String authorizationId) {
        return renderLoginPage(authorizationId, null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Submits the hosted login form.",
            description = "Authenticates the submitted credentials against the pending authorization and " +
                    "redirects the browser to the client's redirect URI carrying the code and state.")
    public Response submitLogin(final MultivaluedMap<String, String> form) {

        final var request = new OidcProviderLoginRequest();
        request.setAuthorizationId(form.getFirst("authorizationId"));
        request.setUsername(form.getFirst("username"));
        request.setPassword(form.getFirst("password"));

        try {
            return Response.seeOther(java.net.URI.create(
                    getOidcProviderAuthorizationService().completeAuthorization(request))).build();
        } catch (RuntimeException ex) {
            // Re-render the form rather than bouncing the browser to a JSON error document. The specific
            // failure reason is deliberately not echoed back.
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_HTML)
                    .entity(renderLoginPage(form.getFirst("authorizationId"), "Invalid username or password."))
                    .build();
        }

    }

    private String renderLoginPage(final String authorizationId, final String error) {

        final var errorHtml = error == null ? "" :
                "<p style=\"color:red\">" + escape(error) + "</p>\n";

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>Sign in</title></head>\n" +
                "<body>\n" +
                "<h1>Sign in</h1>\n" +
                errorHtml +
                "<form method=\"post\" action=\"login\">\n" +
                "  <input type=\"hidden\" name=\"authorizationId\" value=\"" + escape(orEmpty(authorizationId)) + "\"/>\n" +
                "  <label>Username <input type=\"text\" name=\"username\" autofocus/></label><br/>\n" +
                "  <label>Password <input type=\"password\" name=\"password\"/></label><br/>\n" +
                "  <input type=\"submit\" value=\"Sign in\"/>\n" +
                "</form>\n" +
                "</body>\n" +
                "</html>\n";

    }

    private String escape(final String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String orEmpty(final String value) {
        return value == null ? "" : value;
    }

    public OidcProviderAuthorizationService getOidcProviderAuthorizationService() {
        return oidcProviderAuthorizationService;
    }

    @Inject
    public void setOidcProviderAuthorizationService(OidcProviderAuthorizationService oidcProviderAuthorizationService) {
        this.oidcProviderAuthorizationService = oidcProviderAuthorizationService;
    }

}

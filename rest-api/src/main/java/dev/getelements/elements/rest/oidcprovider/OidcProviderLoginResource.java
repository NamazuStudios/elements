package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.model.oidc.OidcProviderConsent;
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
 * The provider's hosted login page. A minimal, server-rendered form. The page carries the pending
 * authorization's opaque id in a hidden field; the POST authenticates the submitted credentials. When the
 * deployment requires consent, the response is the hosted consent screen (posting to {@code oidc/consent});
 * otherwise the browser is redirected to the client's redirect URI with the code and state immediately.
 */
@Path("oidc/login")
public class OidcProviderLoginResource {

    private OidcProviderAuthorizationService oidcProviderAuthorizationService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(
            summary = "Serves the hosted login page for a pending authorization.",
            description = "Renders a minimal credential form bound to the pending authorization. Submitting " +
                    "the form authenticates the user; when consent is required the consent screen follows, " +
                    "otherwise the browser is redirected to the client.")
    public String getLoginPage(@QueryParam("authorizationId") String authorizationId) {
        return renderLoginPage(authorizationId, null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Submits the hosted login form.",
            description = "Authenticates the submitted credentials against the pending authorization. Returns " +
                    "the consent screen when the deployment requires explicit consent, or redirects the " +
                    "browser to the client's redirect URI carrying the code and state.")
    public Response submitLogin(final MultivaluedMap<String, String> form) {

        final var request = new OidcProviderLoginRequest();
        request.setAuthorizationId(form.getFirst("authorizationId"));
        request.setUsername(form.getFirst("username"));
        request.setPassword(form.getFirst("password"));

        final OidcProviderConsent consent;

        try {
            consent = getOidcProviderAuthorizationService().submitLogin(request);
        } catch (RuntimeException ex) {
            // Re-render the form rather than bouncing the browser to a JSON error document. The specific
            // failure reason is deliberately not echoed back.
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_HTML)
                    .entity(renderLoginPage(form.getFirst("authorizationId"), "Invalid username or password."))
                    .build();
        }

        if (consent.isConsentRequired()) {
            return Response.ok(
                    renderConsentPage(
                            consent.getAuthorizationId(),
                            consent.getClientDisplayName(),
                            consent.getScope()),
                    MediaType.TEXT_HTML)
                    .build();
        }

        return seeOther(consent.getRedirectUri());

    }

    /**
     * Renders the hosted consent page, listing the relying party and the scopes being requested.
     */
    static String renderConsentPage(final String authorizationId, final String clientDisplayName, final String scope) {

        final StringBuilder scopeList = new StringBuilder();

        if (scope != null && !scope.isBlank()) {
            scopeList.append("<ul>\n");
            for (final var item : scope.trim().split("\\s+")) {
                scopeList.append("  <li>").append(escape(item)).append("</li>\n");
            }
            scopeList.append("</ul>\n");
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>Grant access</title></head>\n" +
                "<body>\n" +
                "<h1>Grant access</h1>\n" +
                "<p><strong>" + escape(orEmpty(clientDisplayName)) + "</strong> is requesting access to " +
                "your Elements account:</p>\n" +
                scopeList +
                "<form method=\"post\" action=\"consent\">\n" +
                "  <input type=\"hidden\" name=\"authorizationId\" value=\"" + escape(orEmpty(authorizationId)) + "\"/>\n" +
                "  <button type=\"submit\" name=\"decision\" value=\"approve\">Approve</button>\n" +
                "  <button type=\"submit\" name=\"decision\" value=\"deny\">Deny</button>\n" +
                "</form>\n" +
                "</body>\n" +
                "</html>\n";

    }

    static Response seeOther(final String uri) {
        return Response.seeOther(java.net.URI.create(uri)).build();
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

    private static String escape(final String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String orEmpty(final String value) {
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

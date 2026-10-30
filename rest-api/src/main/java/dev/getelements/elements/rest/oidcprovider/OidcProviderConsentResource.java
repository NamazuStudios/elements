package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.service.auth.OidcProviderAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

/**
 * The provider's hosted consent screen. Presented by the hosted login page when the deployment requires
 * explicit consent; the user's decision completes the pending authorization (redirecting back to the
 * relying party with the code) or refuses it (redirecting back with an {@code access_denied} error).
 */
@Path("oidc/consent")
public class OidcProviderConsentResource {

    private OidcProviderAuthorizationService oidcProviderAuthorizationService;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(
            summary = "Records the consent decision for a pending authorization.",
            description = "Approval completes the authorization and redirects the browser to the client's " +
                    "redirect URI carrying the code and state; denial redirects back with an " +
                    "access_denied error per RFC 6749 section 4.1.2.1.")
    public Response submitConsent(final MultivaluedMap<String, String> form) {

        final var authorizationId = form.getFirst("authorizationId");
        final var decision = form.getFirst("decision");

        try {
            return "deny".equals(decision)
                    ? OidcProviderLoginResource.seeOther(
                            getOidcProviderAuthorizationService().denyConsent(authorizationId))
                    : OidcProviderLoginResource.seeOther(
                            getOidcProviderAuthorizationService().approveConsent(authorizationId));
        } catch (final RuntimeException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_HTML)
                    .entity(renderExpiredPage())
                    .build();
        }

    }

    private String renderExpiredPage() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>Authorization expired</title></head>\n" +
                "<body>\n" +
                "<h1>Authorization expired</h1>\n" +
                "<p>This authorization request is no longer valid. Please return to the application and " +
                "sign in again.</p>\n" +
                "</body>\n" +
                "</html>\n";
    }

    public OidcProviderAuthorizationService getOidcProviderAuthorizationService() {
        return oidcProviderAuthorizationService;
    }

    @Inject
    public void setOidcProviderAuthorizationService(OidcProviderAuthorizationService oidcProviderAuthorizationService) {
        this.oidcProviderAuthorizationService = oidcProviderAuthorizationService;
    }

}

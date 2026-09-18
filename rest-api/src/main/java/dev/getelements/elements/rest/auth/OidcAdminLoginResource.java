package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.auth.OidcAdminLoginProvider;
import dev.getelements.elements.sdk.service.auth.OidcAdminLoginService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Publicly-accessible listing of OIDC providers offered as login options on the admin panel's login page.
 * No authentication is required, since the caller has no session yet. See {@link OidcSessionResource} for
 * the login flow itself, and {@link OidcProviderConfigurationResource} for the SUPERUSER-only CRUD API that
 * sets a provider's {@code adminLoginEnabled} flag.
 */
@Path("oidc/admin_login_providers")
public class OidcAdminLoginResource {

    private OidcAdminLoginService oidcAdminLoginService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Lists OIDC providers enabled for admin panel login",
            description = "No authentication required. Returns only providers with adminLoginEnabled set, " +
                    "with just the id and name needed to start a login attempt via POST oidc/session.")
    public List<OidcAdminLoginProvider> getAdminLoginProviders() {
        return getOidcAdminLoginService().getAdminLoginProviders();
    }

    public OidcAdminLoginService getOidcAdminLoginService() {
        return oidcAdminLoginService;
    }

    @Inject
    public void setOidcAdminLoginService(OidcAdminLoginService oidcAdminLoginService) {
        this.oidcAdminLoginService = oidcAdminLoginService;
    }

}

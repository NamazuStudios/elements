package dev.getelements.elements.rest.user;

import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthService;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static dev.getelements.elements.sdk.service.Constants.LINK;

@Path("user/me/link")
public class UserLinkResource {

    private OAuth2AuthService oAuth2AuthService;

    private OidcAuthService oidcAuthService;

    @POST
    @Path("oauth2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Link OAuth2 Identity",
            description = "Links an external OAuth2 identity to the currently authenticated user. " +
                    "Requires an active user session. Returns the updated session information.")
    public SessionCreation linkOAuth2(final OAuth2SessionRequest request) {
        return getOAuth2AuthService().createSession(request);
    }

    @POST
    @Path("oidc")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Link OIDC Identity",
            description = "Links an external OIDC identity to the currently authenticated user. " +
                    "Requires an active user session. Returns the updated session information.")
    public SessionCreation linkOidc(final OidcSessionRequest request) {
        return getOidcAuthService().createSession(request);
    }

    public OAuth2AuthService getOAuth2AuthService() {
        return oAuth2AuthService;
    }

    @Inject
    public void setOAuth2AuthService(@Named(LINK) OAuth2AuthService oAuth2AuthService) {
        this.oAuth2AuthService = oAuth2AuthService;
    }

    public OidcAuthService getOidcAuthService() {
        return oidcAuthService;
    }

    @Inject
    public void setOidcAuthService(@Named(LINK) OidcAuthService oidcAuthService) {
        this.oidcAuthService = oidcAuthService;
    }
}

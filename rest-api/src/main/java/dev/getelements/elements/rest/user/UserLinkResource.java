package dev.getelements.elements.rest.user;

import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.LinkEmailPasswordRequest;
import dev.getelements.elements.sdk.model.user.LinkUsernamePasswordRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OAuth2LinkService;
import dev.getelements.elements.sdk.service.auth.OidcLinkService;
import dev.getelements.elements.sdk.service.user.EmailPasswordLinkService;
import dev.getelements.elements.sdk.service.user.UsernamePasswordLinkService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("user/me/link")
public class UserLinkResource {

    private OAuth2LinkService oAuth2LinkService;

    private OidcLinkService oidcLinkService;

    private EmailPasswordLinkService emailPasswordLinkService;

    private UsernamePasswordLinkService usernamePasswordLinkService;

    @POST
    @Path("oauth2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Link OAuth2 Identity",
            description = "Links an external OAuth2 identity to the currently authenticated user. " +
                    "Requires an active user session. Returns the updated session information.")
    public SessionCreation linkOAuth2(final OAuth2SessionRequest request) {
        return getOAuth2LinkService().createSession(request);
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
        return getOidcLinkService().createSession(request);
    }

    @POST
    @Path("email-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Link Email+Password Credentials",
            description = "Links email+password credentials to the currently authenticated user. " +
                    "The email must first be verified via POST /user/me/email/verify.")
    public User linkEmailPassword(final LinkEmailPasswordRequest request) {
        return getEmailPasswordLinkService().linkEmailPassword(request.getEmail(), request.getPassword());
    }

    public EmailPasswordLinkService getEmailPasswordLinkService() {
        return emailPasswordLinkService;
    }

    @Inject
    public void setEmailPasswordLinkService(EmailPasswordLinkService emailPasswordLinkService) {
        this.emailPasswordLinkService = emailPasswordLinkService;
    }

    @POST
    @Path("username-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Link Username+Password Credentials",
            description = "Links username+password credentials to the currently authenticated user. " +
                    "If the account has no username yet, the supplied username is claimed. " +
                    "If a username is already set it must match; name changes must be done explicitly.")
    public User linkUsernamePassword(final LinkUsernamePasswordRequest request) {
        return getUsernamePasswordLinkService().linkUsernamePassword(request.getUsername(), request.getPassword());
    }

    public UsernamePasswordLinkService getUsernamePasswordLinkService() {
        return usernamePasswordLinkService;
    }

    @Inject
    public void setUsernamePasswordLinkService(UsernamePasswordLinkService usernamePasswordLinkService) {
        this.usernamePasswordLinkService = usernamePasswordLinkService;
    }

    public OAuth2LinkService getOAuth2LinkService() {
        return oAuth2LinkService;
    }

    @Inject
    public void setOAuth2LinkService(OAuth2LinkService oAuth2LinkService) {
        this.oAuth2LinkService = oAuth2LinkService;
    }

    public OidcLinkService getOidcLinkService() {
        return oidcLinkService;
    }

    @Inject
    public void setOidcLinkService(OidcLinkService oidcLinkService) {
        this.oidcLinkService = oidcLinkService;
    }
}

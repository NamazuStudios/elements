package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.session.OidcLoginAttemptCallbackResult;
import dev.getelements.elements.sdk.service.auth.OidcLoginAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The provider-facing redirect target for the browser-redirect OIDC login flow. Registered as each provider's
 * redirect_uri; never called directly by a game client. By default renders a minimal human-readable page and
 * returns 200 with an HTML body — the outcome is communicated to the waiting client via
 * {@link OidcSessionResource}'s poll endpoint, not via this response. If the login attempt was started with a
 * {@code successRedirectUrl}/{@code errorRedirectUrl}, the browser is redirected there instead (302), with this
 * request's own query parameters appended so the destination page has access to the same information.
 */
@Path("oidc/{provider}/callback")
public class OidcCallbackResource {

    private static final Logger logger = LoggerFactory.getLogger(OidcCallbackResource.class);

    private static final String FOOTER_HTML =
"""
        <footer>
            <p>Powered by Namazu Elements</p>
            <p>An Open Source Product of Namazu Studios LLC</p>
            <p><a href="https://github.com/NamazuStudios/">GitHub</a> &middot; <a href="https://namazustudios.com/docs/">Manual</a></p>
        </footer>
""";

    private static final String SUCCESS_HTML =
"""
<html>
    <body>
        <h1>Success</h1>
        <p>You may now close this window and return to the game.</p>
""" + FOOTER_HTML + """
    </body>
</html>
""";

    private static final String FAILURE_HTML =
"""
<html>
    <body>
        <h1>Login failed</h1>
        <p>Please return to the game and try again.</p>
        <ul>
            <li><b>Provider:</b> %s</li>
            <li><b>Code:</b> %s</li>
            <li><b>State:</b> %s</li>
            <li><b>Error:</b> %s</li>
        </ul>
""" + FOOTER_HTML + """
    </body>
</html>
""";

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
                              @QueryParam("error") final String error,
                              @Context final UriInfo uriInfo) {

        if (state == null || (error == null && code == null)) {
            return html(FAILURE_HTML, provider, code, state, error);
        }

        final OidcLoginAttemptCallbackResult result;

        try {
            result = getOidcLoginAttemptService().handleCallback(provider, code, state, error);
        } catch (final Exception ex) {
            // The service layer is responsible for turning every expected failure mode into a result rather
            // than an exception, so this only fires for a genuine bug — there's no redirect target to trust here.
            logger.error("Unexpected error handling OIDC login callback.", ex);
            return html(FAILURE_HTML, provider, code, state, error);
        }

        if (result.getRedirectUrl() != null) {
            return redirect(result.getRedirectUrl(), uriInfo.getRequestUri().getRawQuery(),
                    provider, code, state, error);
        }

        return result.isSuccess() ? html(SUCCESS_HTML) : html(FAILURE_HTML, provider, code, state, error);

    }

    private Response redirect(final String redirectUrl, final String query,
                               final String provider, final String code, final String state, final String error) {

        final var separator = redirectUrl.contains("?") ? "&" : "?";
        final var target = (query == null || query.isBlank()) ? redirectUrl : redirectUrl + separator + query;

        try {
            return Response.status(Response.Status.FOUND).location(new URI(target)).build();
        } catch (final URISyntaxException ex) {
            logger.error("Configured OIDC redirect URL is not a valid URI: {}", redirectUrl, ex);
            return html(FAILURE_HTML, provider, code, state, error);
        }

    }

    private Response html(final String body) {
        return Response.ok(body, MediaType.TEXT_HTML).build();
    }

    private Response html(final String body, final Object ... args) {
        final var formatted = body.formatted(args);
        return Response.ok(formatted, MediaType.TEXT_HTML).build();
    }

    public OidcLoginAttemptService getOidcLoginAttemptService() {
        return oidcLoginAttemptService;
    }

    @Inject
    public void setOidcLoginAttemptService(OidcLoginAttemptService oidcLoginAttemptService) {
        this.oidcLoginAttemptService = oidcLoginAttemptService;
    }

}

package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.auth.CaptchaPublicConfiguration;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyRequest;
import dev.getelements.elements.sdk.model.auth.CaptchaVerifyResponse;
import dev.getelements.elements.sdk.service.auth.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * General-purpose, anonymous CAPTCHA API. Any Elements-backed frontend (admin panel, game clients, other
 * panels) can call these to render the configured CAPTCHA widget and verify a response token, independent of
 * any specific login flow.
 */
@Path("captcha")
public class CaptchaResource {

    private CaptchaService captchaService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets the public CAPTCHA bootstrap configuration",
            description = "Anonymous. Returns whether CAPTCHA is enabled and, if so, the provider and public " +
                    "site key needed to render the widget. Never includes the secret key.")
    public CaptchaPublicConfiguration getPublicConfiguration() {
        return getCaptchaService().getPublicConfiguration();
    }

    @POST
    @Path("verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Verifies a CAPTCHA response token",
            description = "Anonymous. Verifies the supplied token against the configured provider, " +
                    "independent of any specific login flow.")
    public CaptchaVerifyResponse verify(final CaptchaVerifyRequest request) {
        return getCaptchaService().verify(request);
    }

    public CaptchaService getCaptchaService() {
        return captchaService;
    }

    @Inject
    public void setCaptchaService(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

}

package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateCaptchaConfigurationRequest;
import dev.getelements.elements.sdk.service.auth.CaptchaConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("captcha_configuration")
public class CaptchaConfigurationResource {

    private CaptchaConfigurationService captchaConfigurationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets the system-wide CAPTCHA configuration",
            description = "Requires SUPERUSER access. Returns 404 if CAPTCHA has never been configured.")
    public CaptchaConfiguration getConfiguration() {
        return getCaptchaConfigurationService().getConfiguration();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates or updates the system-wide CAPTCHA configuration",
            description = "Requires SUPERUSER access.")
    public CaptchaConfiguration updateConfiguration(final CreateOrUpdateCaptchaConfigurationRequest request) {
        return getCaptchaConfigurationService().updateConfiguration(request);
    }

    public CaptchaConfigurationService getCaptchaConfigurationService() {
        return captchaConfigurationService;
    }

    @Inject
    public void setCaptchaConfigurationService(CaptchaConfigurationService captchaConfigurationService) {
        this.captchaConfigurationService = captchaConfigurationService;
    }

}

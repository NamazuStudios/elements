package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.auth.CreateOrUpdateTotpConfigurationRequest;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;
import dev.getelements.elements.sdk.service.auth.TotpConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("totp_configuration")
public class TotpConfigurationResource {

    private TotpConfigurationService totpConfigurationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets the system-wide TOTP configuration",
            description = "Requires SUPERUSER access. Defaults to disabled if never configured.")
    public TotpConfiguration getConfiguration() {
        return getTotpConfigurationService().getConfiguration();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates or updates the system-wide TOTP configuration",
            description = "Requires SUPERUSER access.")
    public TotpConfiguration updateConfiguration(final CreateOrUpdateTotpConfigurationRequest request) {
        return getTotpConfigurationService().updateConfiguration(request);
    }

    public TotpConfigurationService getTotpConfigurationService() {
        return totpConfigurationService;
    }

    @Inject
    public void setTotpConfigurationService(TotpConfigurationService totpConfigurationService) {
        this.totpConfigurationService = totpConfigurationService;
    }

}

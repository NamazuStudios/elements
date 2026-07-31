package dev.getelements.elements.rest.auth;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcProviderConfigurationRequest;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcProviderConfigurationResponse;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.service.auth.OidcProviderConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("auth_scheme/oidc_provider")
public class OidcProviderConfigurationResource {

    private OidcProviderConfigurationService oidcProviderConfigurationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Lists all OIDC provider configurations",
            description = "Requires SUPERUSER access. Gets a pagination of OIDC provider configurations.")
    public Pagination<OidcProviderConfiguration> getProviderConfigurations(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") final List<String> tags) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return getOidcProviderConfigurationService().getProviderConfigurations(offset, count, tags);
    }

    @GET
    @Path("{providerConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets a specific OIDC provider configuration")
    public OidcProviderConfiguration getProviderConfiguration(
            @PathParam("providerConfigurationId") String providerConfigurationId) {

        providerConfigurationId = Strings.nullToEmpty(providerConfigurationId).trim();

        if (providerConfigurationId.isEmpty()) {
            throw new NotFoundException();
        }

        return getOidcProviderConfigurationService().getProviderConfiguration(providerConfigurationId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new OIDC provider configuration",
            description = "Requires SUPERUSER access. Resolves the discovery document at the supplied " +
                    "discoveryUrl and auto-provisions the matching OIDC auth scheme by issuer, so a single " +
                    "request fully activates a new provider (e.g. Twitch) with no code changes.")
    public CreateOrUpdateOidcProviderConfigurationResponse createProviderConfiguration(
            final CreateOrUpdateOidcProviderConfigurationRequest request) {
        return getOidcProviderConfigurationService().createProviderConfiguration(request);
    }

    @PUT
    @Path("{providerConfigurationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates an OIDC provider configuration")
    public CreateOrUpdateOidcProviderConfigurationResponse updateProviderConfiguration(
            @PathParam("providerConfigurationId") final String providerConfigurationId,
            final CreateOrUpdateOidcProviderConfigurationRequest request) {
        return getOidcProviderConfigurationService().updateProviderConfiguration(providerConfigurationId, request);
    }

    @DELETE
    @Path("{providerConfigurationId}")
    @Operation(summary = "Deletes an OIDC provider configuration")
    public void deleteProviderConfiguration(
            @PathParam("providerConfigurationId") String providerConfigurationId) {

        providerConfigurationId = Strings.nullToEmpty(providerConfigurationId).trim();

        if (providerConfigurationId.isEmpty()) {
            throw new NotFoundException();
        }

        getOidcProviderConfigurationService().deleteProviderConfiguration(providerConfigurationId);
    }

    public OidcProviderConfigurationService getOidcProviderConfigurationService() {
        return oidcProviderConfigurationService;
    }

    @Inject
    public void setOidcProviderConfigurationService(OidcProviderConfigurationService oidcProviderConfigurationService) {
        this.oidcProviderConfigurationService = oidcProviderConfigurationService;
    }

}

package dev.getelements.elements.rest.application;

import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ConfigurationCategory;
import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.application.ApplicationConfigurationService;
import dev.getelements.elements.sdk.service.application.IosApplicationConfigurationService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Handles the management of {@link IosApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Path("application/{applicationNameOrId}/configuration/ios")
public class IosApplicationConfigurationResource {

    private ValidationHelper validationHelper;

    private ApplicationConfigurationService applicationConfigurationService;

    private IosApplicationConfigurationService iosApplicationConfigurationService;

    /**
     * Gets the specific {@link IosApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationNameOrId the application profile name or ID
     *
     * @return the {@link IosApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Gets a iOS Application Configuration",
        description = "Gets a single iOS application based on unique name or ID.")
    public IosApplicationConfiguration getIosApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        return getIosApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    /**
     * Creates a new {@link IosApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param iosApplicationConfiguration the iOS appliation profile object to creates
     *
     * @return the {@link IosApplicationConfiguration} the iOS Application Configuration
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Creates a new iOS ApplicationConfiguration",
        description = "Creates a new iOS ApplicationConfiguration with the specific ID or application.")
    public IosApplicationConfiguration createIosApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final IosApplicationConfiguration iosApplicationConfiguration) {
        getValidationHelper().validateModel(iosApplicationConfiguration);
        return getIosApplicationConfigurationService()
            .createApplicationConfiguration(applicationNameOrId, iosApplicationConfiguration);
    }

    /**
     * Updates an existing {@link IosApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationConfigurationNameOrId the name or identifier of the {@link IosApplicationConfiguration}
     * @param iosApplicationConfiguration the iOS application profile object to update
     *
     * @return the {@link IosApplicationConfiguration} the iOS Application Configuration
     */
    @PUT
    @Path("{applicationConfigurationNameOrId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a iOS ApplicationConfiguration",
            description = "Updates an existing iOS Application profile if it is known to the server.")
    public IosApplicationConfiguration updateIosApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId,
            final IosApplicationConfiguration iosApplicationConfiguration) {
        getValidationHelper().validateModel(iosApplicationConfiguration);
        return getIosApplicationConfigurationService().updateApplicationConfiguration(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                iosApplicationConfiguration);
    }

    /**
     * Deletes an instance of {@link IosApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationConfigurationNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a iOS ApplicationConfiguration",
            description = "Deletes an existing iOS Application profile if it is known to the server.")
    public void deleteIosApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getIosApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @PUT
    @Path("{applicationConfigurationNameOrId}/product_bundles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates the ProductBundle",
            description = "Updates the ProductBundle for the given ApplicationConfiguration")
    public ApplicationConfiguration updateProductBundleForApplicationConfiguration(

            @PathParam("applicationNameOrId")
            final String applicationNameOrId,

            @PathParam("applicationConfigurationNameOrId")
            final String applicationConfigurationNameOrId,

            final List<ProductBundle> productBundles
    ) {

        if (productBundles == null || productBundles.size() == 0) {
            throw new InvalidParameterException("ProductBundles must not be empty.");
        }

        final var applicationConfiguration = getApplicationConfigurationService()
                .updateProductBundles(
                        applicationNameOrId,
                        applicationConfigurationNameOrId,
                        IosApplicationConfiguration.class,
                        productBundles);

        return applicationConfiguration;

    }

    public ApplicationConfigurationService getApplicationConfigurationService() {
        return applicationConfigurationService;
    }

    @Inject
    public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
        this.applicationConfigurationService = applicationConfigurationService;
    }

    public IosApplicationConfigurationService getIosApplicationConfigurationService() {
        return iosApplicationConfigurationService;
    }

    @Inject
    public void setIosApplicationConfigurationService(IosApplicationConfigurationService iosApplicationConfigurationService) {
        this.iosApplicationConfigurationService = iosApplicationConfigurationService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}

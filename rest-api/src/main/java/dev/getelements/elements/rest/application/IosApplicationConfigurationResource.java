package dev.getelements.elements.rest.application;

import dev.getelements.elements.model.application.IosApplicationConfiguration;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.IosApplicationConfigurationService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the management of {@link IosApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Api(
    value = "iOS Application Configuration",
    description = "Operations for the management of ApplicationConfigurations for iOS Applications.",
    authorizations = {@Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)})
@Path("application/{applicationNameOrId}/configuration/ios")
public class IosApplicationConfigurationResource {

    private ValidationHelper validationHelper;

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
    @ApiOperation(
        value = "Gets a iOS Application Configuration",
        notes = "Gets a single iOS application based on unique name or ID.")
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
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Creates a new iOS ApplicationConfiguration",
        notes = "Creates a new iOS ApplicationConfiguration with the specific ID or application.")
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
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a iOS ApplicationConfiguration",
            notes = "Updates an existing iOS Application profile if it is known to the server.")
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
    @ApiOperation(
            value = "Deletes a iOS ApplicationConfiguration",
            notes = "Deletes an existing iOS Application profile if it is known to the server.")
    public void deleteIosApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getIosApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
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

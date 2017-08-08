package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.rest.EnhancedApiListingResource;
import com.namazustudios.socialengine.service.FacebookApplicationConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

/**
 * Created by patricktwohig on 6/14/17.
 */
@Api(value = "Facebook Application Configuration",
     description = "Operations for the management of ApplictionConfiguration instances for Facebook Applications.",
     authorizations = {@Authorization(EnhancedApiListingResource.FACBOOK_OAUTH_KEY)})
@Path("application/{applicationNameOrId}/configuration/facebook")
public class FacebookApplicationConfigurationResource {

    private ValidationHelper validationHelper;

    private FacebookApplicationConfigurationService facebookApplicationConfigurationService;

    /**
     * Gets the specific {@link FacebookApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationNameOrId the application profile name or ID
     *
     * @return the {@link FacebookApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a Facebook Application Configuration",
            notes = "Gets a single Facebook application based on unique name or ID.")
    public FacebookApplicationConfiguration getFacebookApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        return getFacebookApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    /**
     * Creates a new {@link FacebookApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param facebookApplicationConfiguration the Facebook appliation profile object to creates
     *
     * @return the {@link FacebookApplicationConfiguration} the Facebook Application Configuration
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a new Facebook ApplicationConfiguration",
            notes = "Creates a new Facebook ApplicationConfiguration with the specific ID or application.")
    public FacebookApplicationConfiguration createFacebookApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        getValidationHelper().validateModel(facebookApplicationConfiguration);

        if (Objects.equals(facebookApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
            Objects.equals(facebookApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getFacebookApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, facebookApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }

    }

    /**
     * Updates an existing {@link FacebookApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationConfigurationNameOrId the name or identifier of the {@link FacebookApplicationConfiguration}
     * @param facebookApplicationConfiguration the Facebook application profile object to update
     *
     * @return the {@link FacebookApplicationConfiguration} the Facebook Application Configuration
     */
    @PUT
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a Facebook ApplicationConfiguration",
            notes = "Updates an existing Facebook Application profile if it is known to the server.")
    public FacebookApplicationConfiguration updateApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId,
            final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        getValidationHelper().validateModel(facebookApplicationConfiguration);

        if (Objects.equals(facebookApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
            Objects.equals(facebookApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getFacebookApplicationConfigurationService().updateApplicationConfiguration(
                    applicationNameOrId,
                    applicationConfigurationNameOrId,
                    facebookApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }

    }

    /**
     * Deletes an instance of {@link FacebookApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationConfigurationNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a Facebook ApplicationConfiguration",
            notes = "Deletes an existing Facebook Application profile if it is known to the server.")
    public void deleteFacebookApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getFacebookApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public FacebookApplicationConfigurationService getFacebookApplicationConfigurationService() {
        return facebookApplicationConfigurationService;
    }

    @Inject
    public void setFacebookApplicationConfigurationService(FacebookApplicationConfigurationService facebookApplicationConfigurationService) {
        this.facebookApplicationConfigurationService = facebookApplicationConfigurationService;
    }

}

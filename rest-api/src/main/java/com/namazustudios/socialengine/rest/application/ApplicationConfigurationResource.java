package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.ProductBundle;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.service.ApplicationConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

/**
 * Created by patricktwohig on 7/13/15.
 */
@Api(value = "Application Configurations",
    description = "Manages application profiles.  An application profile is a collection of " +
                   "application metadata for a particular configuration of deployment.  For example, " +
                   "an application may be deployed on both Android and iOS.  One application profile" +
                   "each for Android and iOS would be required.",
    authorizations = {@Authorization(SESSION_SECRET)})
@Path("application/{applicationNameOrId}/configuration")
public class ApplicationConfigurationResource {

    private ApplicationConfigurationService applicationConfigurationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Searches application profiles",
                  notes = "Searches all instances of ApplicationProfiles associated with " +
                          "the application.  The search query may be a full text search.")
    public Pagination<ApplicationConfiguration> getApplicationProfiles(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getApplicationConfigurationService().getApplicationProfiles(applicationNameOrId, offset, count) :
            getApplicationConfigurationService().getApplicationProfiles(applicationNameOrId, offset, count, search);

    }

    @PUT
    @Path("{applicationConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates the ProductBundle",
                  notes = "Updates the ProductBundle for the given ApplicationConfiguration")
    public ApplicationConfiguration updateProductBundleForApplicationConfiguration(
            @PathParam("applicationConfigurationId") final String applicationConfigurationId,
            final ProductBundle productBundle
    ) {

    }



    public ApplicationConfigurationService getApplicationConfigurationService() {
        return applicationConfigurationService;
    }

    @Inject
    public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
        this.applicationConfigurationService = applicationConfigurationService;
    }

}

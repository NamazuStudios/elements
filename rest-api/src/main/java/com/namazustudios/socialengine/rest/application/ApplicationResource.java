package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.service.ApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by patricktwohig on 7/9/15.
 */
@Api(value = "Applications",
     description = "These operations manage any variety of client side applications which " +
                   "may be communicating with the server.  This stores minimal information " +
                   "for each and is used primairly as an aggregation point for other application " +
                   "profiles.  Application metadata is typically used for client side apps to determine " +
                   "the latest version or to resolve any compatiblity issues.  This can also be used to " +
                   "perform force upgrades.",
     authorizations = {@Authorization(EnhancedApiListingResource.SESSION_SECRET)})
@Path("application")
public class ApplicationResource {

    private ApplicationService applicationService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Applications",
                  notes = "Performs a full-text search of all applications known to the server.  As with " +
                          "other full-text endpoints this allows for pagination and offset.")
    public Pagination<Application> getApplications(
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
            getApplicationService().getApplications(offset, count) :
            getApplicationService().getApplications(offset, count, query);

    }

    @GET
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get an Application",
                  notes = "Gets the metadata for a single application.  This may include more specific " +
                          "details not availble in the bulk-get or fetch operation.")
    public Application getApplication(
            @PathParam("nameOrId") final String nameOrId) {
        return getApplicationService().getApplication(nameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a New Application",
                  notes = "Gets the metadata for a single application.  This may include more specific " +
                          "details not available in the bulk-get or fetch operation.")
    public Application createApplication(final Application application) {
        getValidationHelper().validateModel(application);
        return getApplicationService().createApplication(application);
    }

    @PUT
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates an Application",
                  notes = "Performs an update to an existing application known to the server.")
    public Application updateApplication(
            @PathParam("nameOrId") final String nameOrId,
            final Application application) {
        getValidationHelper().validateModel(application);
        return getApplicationService().updateApplication(nameOrId, application);
    }

    @DELETE
    @Path("{nameOrId}")
    @ApiOperation(value = "Deletes an Application",
                  notes = "Deletes a specific application known to the server.")
    public void deleteApplication(
            @PathParam("nameOrId") final String nameOrId) {
        getApplicationService().deleteApplication(nameOrId);
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Inject
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}

package com.namazustudios.socialengine.rest;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 7/9/15.
 */
@Api(value = "Applications",
     description = "These operations manage any variety of client side applications which " +
                   "may be communicating with the server.  This stores minimal information " +
                   "for each and is used primairly as an aggregation point for other application " +
                   "profiles.  Application metadata is typically used for client side apps to determine " +
                   "the latest version or to resolve any compatiblity issues.  This can also be used to " +
                   "perform force upgrades.")
@Path("application")
public class ApplicationResource {

    @Inject
    private ApplicationService applicationService;

    @Inject
    private ValidationHelper validationService;

    @GET
    @Path("{offset}/{count}/{search}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Applications",
                  notes = "Performs a full-text search of all applications known to the server.  As with " +
                          "other full-text endpoints this allows for pagination and offset.")
    public Pagination<Application> searchApplications(
            @PathParam("offset") final int offset,
            @PathParam("count")  final int count,
            @PathParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String searchQuery = Strings.nullToEmpty(search).trim();
        return applicationService.getApplications(offset, count, searchQuery);

    }

    @GET
    @Path("{offset}/{count}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Applications",
            notes = "Lists all Applications known to the server in the default order.  Provides " +
                    "no filtering by full text.")
    public Pagination<Application> getApplications(
            @PathParam("offset") @DefaultValue("0")  final int offset,
            @PathParam("count")  @DefaultValue("20") final int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return applicationService.getApplications(offset, count);

    }

    @GET
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get an Application",
                  notes = "Gets the metadata for a single application.  This may include more specific " +
                          "details not availble in the bulk-get or fetch operation.")
    public Application getApplication(@PathParam("nameOrId") final String nameOrId) {
        return applicationService.getApplication(nameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a New Application",
                  notes = "Gets the metadata for a single application.  This may include more specific " +
                          "details not available in the bulk-get or fetch operation.")
    public Application createApplication(final Application application) {
        validationService.validateModel(application);
        return applicationService.createApplication(application);
    }

    @PUT
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates an Application",
                  notes = "Performs an update to an existing application known to the server.")
    public Application updateApplication(@PathParam("nameOrId") final String nameOrId, final Application application) {
        validationService.validateModel(application);
        return applicationService.updateApplication(nameOrId, application);
    }

    @DELETE
    @Path("{nameOrId}")
    @ApiOperation(value = "Deletes an Application",
                  notes = "Deletes a specific application known to the server.")
    public void deleteApplication(@PathParam("nameOrId") final String nameOrId) {
        applicationService.deleteApplication(nameOrId);
    }

}

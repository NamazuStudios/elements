package com.namazustudios.socialengine.rest;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 7/9/15.
 */
@Path("application")
public class ApplicationResource {

    @POST
    public Application createApplication(final Application application) {
        return null;
    }

    @Inject
    private ApplicationService applicationService;

    @Inject
    private ValidationHelper validationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Pagination<Application> getApplications(
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") @DefaultValue("")   final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String searchQuery = Strings.nullToEmpty(search).trim();

        return searchQuery.isEmpty() ?
            applicationService.getApplications(offset, count) :
            applicationService.getApplications(offset, count, searchQuery);

    }

    @GET
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Application getApplication(@PathParam("nameOrId") final String nameOrId) {
        return applicationService.getApplication(nameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Application createApplciation(final Application application) {
        validationService.validateModel(application);
        return applicationService.createApplication(application);
    }

    @PUT
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Application updateApplication(@PathParam("nameOrId") final String nameOrId, final Application application) {
        validationService.validateModel(application);
        return applicationService.updateApplication(nameOrId, application);
    }

    @DELETE
    @Path("{nameOrId}")
    public void deleteApplication(@PathParam("nameOrId") final String nameOrId) {
        applicationService.deleteApplication(nameOrId);
    }

}

package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;

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

        return null;

    }

    @GET
    @Path("{name}")
    public Application getApplication(@PathParam("name") final String name) {
        return null;
    }

    @PUT
    @Path("{name}")
    public Application updateApplication(@PathParam("name") final String name, final Application application) {
        return null;
    }

    @DELETE
    @Path("{name}")
    public void deleteApplication(@PathParam("name") final String name, final Application application) {

    }

}

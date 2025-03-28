package dev.getelements.elements.rest.application;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.CreateApplicationRequest;
import dev.getelements.elements.sdk.model.application.UpdateApplicationRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.application.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by patricktwohig on 7/9/15.
 */
@Path("application")
public class ApplicationResource {

    private ApplicationService applicationService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Search Applications",
            description = "Performs a full-text search of all applications known to the server.  As with " +
                          "other full-text endpoints this allows for pagination and offset."
    )
    public Pagination<Application> getApplications(
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive summary.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive summary.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getApplicationService().getApplications(offset, count) :
            getApplicationService().getApplications(offset, count, query);

    }

    @GET
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get an Application",
                  description = "Gets the metadata for a single application.  This may include more specific " +
                          "details not available in the bulk-get or fetch operation.")
    public Application getApplication(
            @PathParam("nameOrId") final String nameOrId) {
        return getApplicationService().getApplication(nameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates a New Application",
                  description = "Gets the metadata for a single application.  This may include more specific " +
                          "details not available in the bulk-get or fetch operation.")
    public Application createApplication(final CreateApplicationRequest applicationRequest) {
        getValidationHelper().validateModel(applicationRequest);
        return getApplicationService().createApplication(applicationRequest);
    }

    @PUT
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates an Application",
                  description = "Performs an update to an existing application known to the server.")
    public Application updateApplication(
            @PathParam("nameOrId") final String nameOrId,
            final UpdateApplicationRequest applicationRequest) {
        getValidationHelper().validateModel(applicationRequest);
        return getApplicationService().updateApplication(nameOrId, applicationRequest);
    }

    @DELETE
    @Path("{nameOrId}")
    @Operation(summary = "Deletes an Application",
                  description = "Deletes a specific application known to the server.")
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

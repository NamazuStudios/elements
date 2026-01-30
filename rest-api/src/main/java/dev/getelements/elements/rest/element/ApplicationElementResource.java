package dev.getelements.elements.rest.element;

import dev.getelements.elements.sdk.model.application.ApplicationStatus;
import dev.getelements.elements.sdk.service.application.ApplicationStatusService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("elements/application")
public class ApplicationElementResource {

    private ApplicationStatusService applicationStatusService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets the status of all applications."
    )
    public List<ApplicationStatus> getAllApplicationStatuses() {
        return getApplicationStatusService().getAllDeployments();
    }

    public ApplicationStatusService getApplicationStatusService() {
        return applicationStatusService;
    }

    @Inject
    public void setApplicationStatusService(ApplicationStatusService applicationStatusService) {
        this.applicationStatusService = applicationStatusService;
    }

}

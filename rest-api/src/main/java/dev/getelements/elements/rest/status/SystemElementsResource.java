package dev.getelements.elements.rest.status;

import dev.getelements.elements.sdk.model.system.ElementMetadata;
import dev.getelements.elements.sdk.service.application.ApplicationStatusService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("elements/system")
public class SystemElementsResource {

    private ApplicationStatusService applicationStatusService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets the status of all applications."
    )
    public List<ElementMetadata> getAllInstalledElements() {
        return getApplicationStatusService().getAllSystemElements();
    }

    public ApplicationStatusService getApplicationStatusService() {
        return applicationStatusService;
    }

    @Inject
    public void setApplicationStatusService(ApplicationStatusService applicationStatusService) {
        this.applicationStatusService = applicationStatusService;
    }

}

package dev.getelements.elements.rest.element;

import dev.getelements.elements.sdk.model.system.ElementContainerStatus;
import dev.getelements.elements.sdk.model.system.ElementMetadata;
import dev.getelements.elements.sdk.model.system.ElementRuntimeStatus;
import dev.getelements.elements.sdk.model.system.ElementSpi;
import dev.getelements.elements.sdk.service.system.ElementStatusService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("elements")
@Schema(description = "Provides information on the system's available Elements.")
public class ElementStatusResource {

    private ElementStatusService elementStatusService;

    @GET
    @Path("system")
    @Produces(MediaType.APPLICATION_JSON)
    @Schema(description = "Lists all system-defined Elements.")
    public List<ElementMetadata> getAllSystemElements() {
        return getElementStatusService().getAllSystemElements();
    }

    @GET
    @Path("runtime")
    @Produces(MediaType.APPLICATION_JSON)
    @Schema(description = "Lists all loaded Elements in the runtime.")
    public List<ElementRuntimeStatus> getAllRuntimes() {
        return getElementStatusService().getAllRuntimes();
    }

    @GET
    @Path("container")
    @Produces(MediaType.APPLICATION_JSON)
    @Schema(description = "Lists all loaded Elements that are in containers.")
    public List<ElementContainerStatus> getAllContainers() {
        return getElementStatusService().getAllContainers();
    }

    @GET
    @Path("builtin_spi")
    @Produces(MediaType.APPLICATION_JSON)
    @Schema(description = "Lists all builtin SPIs supported by this installation of Elements.")
    public List<ElementSpi> getAllBuiltinSpis() {
        return getElementStatusService().getAllBuiltinSpis();
    }

    public ElementStatusService getElementStatusService() {
        return elementStatusService;
    }

    @Inject
    public void setElementStatusService(ElementStatusService elementStatusService) {
        this.elementStatusService = elementStatusService;
    }

}

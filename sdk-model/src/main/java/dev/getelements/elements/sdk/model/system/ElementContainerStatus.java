package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Indicates the status of an application deployment.
 *
 * @param status the status
 * @param uris the uris serving
 * @param logs the logs of the deployment
 * @param elements the elements associated with the deployment
 */
@Schema(description =
        "Provides the status for containers running within one or more Elements. A container is registered with the " +
        "main server and exposes endpoints via the listed URIs. Only Elements serving data will be visible here."
)
public record ElementContainerStatus(

        @Schema(description = "The runtime associated with the container.")
        ElementRuntimeStatus runtime,

        @Schema(description = "A string indicating the status of the container.")
        String status,

        @Schema(description = "The URIs served by this container.")
        Set<URI> uris,

        @Schema(description = "A list of logs related to the deployment of the container.")
        List<String> logs,

        @Schema(description = "A list of Elements included in the container.")
        List<ElementMetadata> elements

) {}

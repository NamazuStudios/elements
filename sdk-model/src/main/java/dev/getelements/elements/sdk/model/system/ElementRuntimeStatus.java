package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description =
        "Reports the status of an element runtime, which is a deployed in-memory Element. An in-memory " +
        "Element may or may not expose endpoints but can still be loaded in memory."
)
public record ElementRuntimeStatus(

        @Schema(description = "The deployment associated with this runtime status.")
        ElementDeployment deployment,

        @Schema(description = "The status of the runtime.")
        String status,

        @Schema(description = "A readout of temporary files that are associated with the deployment.")
        List<String> tempFiles,

        @Schema(description = "A list of logs related to the deployment of the runtime.")
        List<String> logs,

        @Schema(description = "A list of Elements included in the runtime.")
        List<ElementMetadata> elements

) {}

package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description =
        "Defines a package of Elements from a single ELM artifact with per-path attribute configuration. " +
        "This allows multiple element paths within an ELM to have different attribute configurations."
)
public record ElementPackageDefinition(

        @Schema(description =
                "The ELM artifact coordinate to resolve and deploy. This ELM file will be downloaded, extracted, " +
                "and its contents organized into the deployment directory structure."
        )
        String elmArtifact,

        @Schema(description =
                "Map of element paths to their custom attributes. The key is the path on disk where the element " +
                "will be deployed (matching the subdirectory structure within the ELM), and the value is a map " +
                "of custom attributes to pass to that specific element at load time via the AttributesLoader mechanism."
        )
        Map<String, Map<String, Object>> pathAttributes

) {
}
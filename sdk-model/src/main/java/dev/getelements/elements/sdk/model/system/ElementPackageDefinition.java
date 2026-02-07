package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
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
                "Map of element paths to a custom SPI class paths. This allows for an individual SPI specification " +
                "for each Element contained within the ELM file in the specified ELM artifact."
        )
        Map<String, List<String>> pathSpiClassPaths,

        @Schema(description =
                "Map of element paths to their custom attributes. The key is the inside the ELM for each Element " +
                "inside the ELM file. and the value is a map of custom attributes to pass to that specific element " +
                "at load time via the AttributesLoader mechanism."
        )
        Map<String, Map<String, Object>> pathAttributes

) {}

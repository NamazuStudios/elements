package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description =
        "Defines the classpath configuration for Elements within a deployment. Can specify either Maven " +
        "artifact coordinates (api, spi, element lists) or a single ELM artifact coordinate."
)
public record ElementPathDefinition(

        @Schema(description =
                "List of API artifact coordinates. These artifacts will be loaded into a shared API classloader " +
                "accessible to all Elements. Ignored if elmArtifact is specified."
        )
        List<String> apiArtifacts,

        @Schema(description =
                "List of SPI (Service Provider Implementation) artifact coordinates. These provide the " +
                "implementation of Element framework services."
        )
        List<String> spiArtifacts,

        @Schema(description =
                "List of Element implementation artifact coordinates. These contain the actual Element code. " +
                "Ignored if elmArtifact is specified."
        )
        List<String> elementArtifacts,

        @Schema(description =
                "A single ELM artifact coordinate. If specified, this ELM will be used instead of the " +
                "apiArtifacts, spiArtifacts, and elementArtifacts lists. The ELM contains all necessary " +
                "API, SPI, and Element code."
        )
        String elmArtifact,

        @Schema(description =
                "The path on disk where this element will be deployed within the deployment directory structure. " +
                "This path is used as the subdirectory name for organizing element artifacts when expanded from " +
                "an ELM file."
        )
        String path,

        @Schema(description =
                "Custom attributes specific to this element path. These key-value pairs will be passed to the " +
                "Element at load time via the AttributesLoader mechanism, allowing per-element configuration " +
                "beyond the deployment-wide attributes."
        )
        Map<String, Object> attributes

) {
}

package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description =
        "Defines the classpath configuration for Elements within a deployment. Can specify either Maven " +
        "artifact coordinates (api, spi, element lists) or a single ELM artifact coordinate."
)
public record ElementDefinition(

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
        String elmArtifact

) {
}

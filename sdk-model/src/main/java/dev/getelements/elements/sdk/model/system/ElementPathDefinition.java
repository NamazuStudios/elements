package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Map;

@Schema(description =
        "Defines the classpath configuration for Elements within a deployment. Can specify either Maven " +
        "artifact coordinates (api, spi, element lists) or a single ELM artifact coordinate."
)
public record ElementPathDefinition(

        @Pattern(regexp = "[^/\\\\]+", message = "path must be a single path segment with no '/' or '\\' characters")
        @Schema(description =
                "The single directory name where this element will be deployed within the deployment directory " +
                "structure. Must not contain '/' as it represents a single path segment. This is optional and can " +
                "be assigned at deployment time."
        )
        String path,

        @Schema(description =
                "List of API artifact coordinates. These artifacts will be loaded into a shared API classloader " +
                "accessible to all Elements. Ignored if elmArtifact is specified."
        )
        List<String> apiArtifacts,

        @Schema(description =
                "List of SPI (Service Provider Implementation) builtins. These provide the implementation of Element " +
                "framework services."
        )
        List<String> spiBuiltins,

        @Schema(description =
                "List of SPI (Service Provider Implementation) artifact coordinates. These provide the " +
                "implementation of Element framework services."
        )
        List<String> spiArtifacts,

        @Schema(description =
                "List of Element implementation artifact coordinates. These contain the actual Element code. "
        )
        List<String> elementArtifacts,

        @Schema(description =
                "Custom attributes specific to this element path. These key-value pairs will be passed to the " +
                "Element at load time via the AttributesLoader mechanism, allowing per-element configuration " +
                "beyond the deployment-wide attributes."
        )
        Map<String, Object> attributes

) {
        /**
         * Canonical constructor ensuring all collections are immutable copies.
         */
        public ElementPathDefinition {
                apiArtifacts = apiArtifacts == null ? null : List.copyOf(apiArtifacts);
                spiBuiltins = spiBuiltins == null ? null : List.copyOf(spiBuiltins);
                spiArtifacts = spiArtifacts == null ? null : List.copyOf(spiArtifacts);
                elementArtifacts = elementArtifacts == null ? null : List.copyOf(elementArtifacts);
                attributes = attributes == null ? null : Map.copyOf(attributes);
        }
}

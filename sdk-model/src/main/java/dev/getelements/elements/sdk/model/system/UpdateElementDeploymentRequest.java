package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Schema(description =
        "Request to update a new Element with specified artifacts and their repositories. All artifacts specified " +
        "must exist in the repositories provided. Additionally, the repositories must include any dependencies " +
        "required by the artifacts and all dependencies of those dependencies, and so on. The system will resolve " +
        "all dependencies transitively."
)
public record UpdateElementDeploymentRequest(

        @Valid
        @Schema(description =
                "List of Element definitions specifying the classpaths and artifacts for each Element to deploy. " +
                "Each definition can specify either Maven artifact coordinates (API, SPI, Element lists) or a " +
                "single ELM artifact coordinate."
        )
        List<ElementPathDefinition> elements,

        @Valid
        @Schema(description =
                "List of Element package definitions specifying ELM artifacts to deploy with per-path attribute " +
                "configuration. Each package can contain multiple elements with individualized attribute mappings."
        )
        List<ElementPackageDefinition> packages,

        @Schema(description =
                "Flag indicating whether to use the default artifact repositories in addition to any provided. The " +
                "actual repositories included will be those defined by the system's default configuration at " +
                "deployment time."
        )
        boolean useDefaultRepositories,

        @Valid
        @Schema(description =
                "List of artifact repositories to use for resolving the specified artifacts and their dependencies. " +
                "All artifacts and their dependencies must be found within these repositories.")
        List<ElementArtifactRepository> repositories,

        @Schema(description =
                "Map of element paths to their custom attributes. The key is the path inside the ELM for each Element " +
                "in the ELM file, and the value is a map of custom attributes to pass to that specific element " +
                "at load time via the AttributesLoader mechanism."
        )
        Map<String, Map<String, Object>> pathAttributes,

        @Schema(description =
                "Map of element paths to builtin SPI configurations. This allows for an individual SPI specification " +
                "for each Element contained within the ELM file in the large object."
        )
        Map<String, List<String>> pathSpiBuiltins,

        @Schema(description =
                "Map of element paths to a custom SPI class paths. This allows for an individual SPI specification " +
                "for each Element contained within the ELM file in the large object."
        )
        Map<String, List<String>> pathSpiClassPaths,

        @NotNull
        @Schema(description =
                "Sets the state of the deployment. When updating, the change will only take place if the deployment " +
                "meets all the requirements. For example, it will not be possible to put the deployment in the " +
                "ENABLED or DISABLED state if there is not enough code loaded to attempt to load an Element."
        )
        ElementDeploymentState state

) implements ElementDeploymentRequest {}

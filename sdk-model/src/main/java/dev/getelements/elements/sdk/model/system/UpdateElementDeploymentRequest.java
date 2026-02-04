package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ArtifactRepository;
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
        List<ElementDefinition> elements,

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
                "Custom attributes to pass to the Element at load time. These key-value pairs will be merged with " +
                "any default attributes and made available to the Element during initialization."
        )
        Map<String, Object> attributes,

        @NotNull
        @Schema(description =
                "Sets the state of the deployment. When updating, the change will only take place if the deployment " +
                "meets all the requirements. For example, it will not be possible to put the deployment in the " +
                "ENABLED or DISABLED state if there is not enough code loaded to attempt to load an Element."
        )
        ElementDeploymentState state

) implements ElementDeploymentRequest {}

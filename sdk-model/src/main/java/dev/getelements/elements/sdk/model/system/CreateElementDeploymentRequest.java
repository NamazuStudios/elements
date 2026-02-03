package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ArtifactRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@Schema(description =
        "Request to create a new Element with specified artifacts and their repositories. All artifacts specified " +
        "must exist in the repositories provided. Additionally, the repositories must include any dependencies " +
        "required by the artifacts and all dependencies of those dependencies, and so on. The system will resolve " +
        "all dependencies transitively."
)
public record CreateElementDeploymentRequest(

        @Schema(description =
                "The application name or ID. May be null. If null, then the Element will be scoped to the " +
                "global or root element registry making it visible to all Applications. If specific to an " +
                "Application, then this will be be visible only to Elements within that Application."
        )
        String applicationNameOrId,

        @Schema(description =
                "List of API artifact identifiers to include in the Element. These will be shared system wide " +
                "available to all Elements installed within the scope of the Element."
        )
        List<String> apiArtifacts,

        @Schema(description =
                "List of SPI (Service Provider Implementation) artifact identifiers to include in the Element. For " +
                "most use cases, SPI artifacts are required. The requested SPI must be compatible with the requested " +
                "API, Namazu Elements version, and the Element itself. Technically, this can be blank or null which " +
                "means that the SPIs must appear embedded in the Element itself which is not recommended practice."
        )
        List<String> spiArtifacts,

        @Schema(description =
                "A single ELM artifact identifier to include in the Element. Invalid if using general element " +
                "artifacts."
        )
        String elmArtifact,

        @Schema(description =
                "The list of general element artifact identifiers to include in the Element. Invalid if using an " +
                "ELM artifact as an ELM artifact can encapsulate the entire element definition including " +
                "dependencies. May be empty or null."
        )
        List<String> elementArtifacts,

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
        List<ArtifactRepository> repositories,

        @Schema(description =
                "Custom attributes to pass to the Element at load time. These key-value pairs will be merged with " +
                "any default attributes and made available to the Element during initialization."
        )
        Map<String, Object> attributes,

        @Schema(description =
                "Sets the state of the deployment. When creating, the creation will only take place if the " +
                "deployment meets all the requirements. For example, it will not be possible to put the deployment " +
                "in the ENABLED or DISABLED state if there is not enough code loaded to attempt to load an Element. " +
                "If this field is left null, then the state will be inferred by the system and favor immediately " +
                "setting the state to ENABLED."
        )
        ElementDeploymentState state


) implements ElementDeploymentRequest {}

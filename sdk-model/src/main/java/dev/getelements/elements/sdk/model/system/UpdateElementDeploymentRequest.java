package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ArtifactRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jdk.jfr.Threshold;

import java.util.List;

@Schema(description =
        "Request to update a new Element with specified artifacts and their repositories. All artifacts specified " +
        "must exist in the repositories provided. Additionally, the repositories must include any dependencies " +
        "required by the artifacts and all dependencies of those dependencies, and so on. The system will resolve " +
        "all dependencies transitively."
)
public record UpdateElementDeploymentRequest(

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
                "artifacts. May be empty or null."
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

        @NotNull
        @Pattern(regexp = "ENABLED|DISABLED")
        @Schema(description =
                "Sets the state of the deployment. When updating, the change will only take place if the deployment " +
                "meets all the requirements. For example, it will not be possible to put the deployment in the " +
                "ENABLED or DISABLED state if there is not enough code loaded to attempt to load an Element."
        )
        ElementDeploymentState state

)
{}

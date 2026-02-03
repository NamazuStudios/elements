package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectState;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.Map;

@Schema(description =
        "Represents the deployment configuration for an Element within the system. This includes details about " +
        "the application context, artifacts to be included, and repositories for artifact resolution."
)
public record ElementDeployment(

        @Null(groups = ValidationGroups.Insert.class)
        @NotNull(groups = ValidationGroups.Update.class)
        @Schema(description =
                "The database unique identifier of the Element deployment."
        )
        String id,

        @Schema(description =
                "The application context under which the Element is being deployed. If null, the Element is " +
                "deployed system wide."
        )
        Application application,

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
                "The list of general element artifact identifiers to include in the Element. Invalid if using an " +
                "ELM artifact as an ELM artifact can encapsulate the entire element definition including " +
                "dependencies. May be empty or null."
        )
        List<String> elementArtifacts,

        @Valid
        @Schema(description =
                "The large object which houses the actual ELM file. The elm file may have contents indicating that " +
                "the Element will be loaded from the LargeObject instead of the artifact or classpath specifier."
        )
        LargeObjectReference elm,

        @Schema(description =
                "A single ELM artifact identifier to include in the Element. Invalid if using general element " +
                "artifacts or an elm file."
        )
        String elmArtifact,

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
                "The state of the deployment. Only deployments in the ENABLED state will be deployed to nodes in the " +
                "system."
        )
        ElementDeploymentState state,

        @Schema(description =
                "Monotonically increasing version number, updated on each modification. Used to detect changes " +
                "without comparing all fields."
        )
        long version

) implements ElementDeploymentRequest {

        /**
         * In addition to the existing logic, this checks that the {@link LargeObject} contains content.
         *
         * @return true if ready
         */
        @Override
            public boolean isReady() {
                return ElementDeploymentRequest.super.isReady() ||
                       elm() != null && LargeObjectState.UPLOADED.equals(elm().getState());
            }

}

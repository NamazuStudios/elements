package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectState;
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

        @Valid
        @Schema(description =
                "The large object which houses the actual ELM file. If present, Elements can be loaded from " +
                "this uploaded ELM file in addition to any Element definitions."
        )
        LargeObjectReference elm,

        @Schema(description =
                "Map of element paths to their custom attributes. The key is the path inside the ELM for each Element " +
                "in the ELM file, and the value is a map of custom attributes to pass to that specific element " +
                "at load time via the AttributesLoader mechanism."
        )
        Map<String, Map<String, Object>> pathAttributes,

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
         * A deployment is ready if it has Element definitions or an uploaded ELM file.
         *
         * @return true if ready
         */
        @Override
            public boolean ready() {
                return ElementDeploymentRequest.super.ready() ||
                       (elm() != null && LargeObjectState.UPLOADED.equals(elm().getState()));
            }

}

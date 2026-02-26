package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description =
        "Defines an artifact repository with its identifier and URL. Artifact repositories are used to store and " +
        "retrieve Java artifacts such as libraries, and dependencies, and other build outputs. Namazu Elements " +
        "permits the use of multiple artifact repositories to resolve artifacts required for Elements."
)
public record ElementArtifactRepository(

        @Schema(description =
                "The identifier of the repository. This is typically a unique name used to reference the " +
                "repository when providing credentials.")
        String id,

        @Schema(description =
                "This is the URL for the repository.")
        String url) {}

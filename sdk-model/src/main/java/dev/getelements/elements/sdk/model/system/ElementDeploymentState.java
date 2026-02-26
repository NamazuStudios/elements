package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents the state of a particular.
 */
@Schema(description = "The state of the deployment.")
public enum ElementDeploymentState {

    /**
     * The deployment is created but does not have the code loaded.
     */
    @Schema(description = "The deployment exists but does not have the code loaded.")
    UNLOADED,

    /**
     * The deployment is created, has all code necessary to load, and will be loaded as soon as possible.
     */
    @Schema(description =
            "The deployment is created, has all code necessary to load, and will be loaded as soon as possible."
    )
    ENABLED,

    /**
     * The deployment is created, has all code necessary to load, but will be skipped and unloaded as soon as possible.
     */
    @Schema(description =
            "The deployment is created, has all code necessary to load, but will be skipped and unloaded as soon as " +
            "possible."
    )
    DISABLED

}

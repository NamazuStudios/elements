package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reports a particular permitted feature of the system and what can be exposed to Elements")
public record ElementFeature(

        @Schema(description = "The name of the feature. Used as an internal reference.")
        String name,

        @Schema(description = "A brief description of what it permits to the Elements.")
        String description) {}

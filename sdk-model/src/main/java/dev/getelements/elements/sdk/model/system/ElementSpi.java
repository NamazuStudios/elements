package dev.getelements.elements.sdk.model.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Maps a well known SPI Implementation ID to specific artifacts supported by the currently running instance of
 * Namazu Elements. This is used to ensure compatibility across version of and deployments of Namazu Elements.
 *
 * @param id the well known ID
 * @param version the version compatible with this set of artifacts
 * @param coordinates the artifacts themselves
 */
@Schema(description = "An Element Loader SPI Implementation.")
public record ElementSpi(

        @NotNull
        @Schema(description = "The well known ID.")
        String id,

        @NotNull
        @Schema(description = "The version which is compatible with the specified artifacts.")
        String version,

        @NotNull
        @Schema(description = "Briefly describes this spi and its intended use case.")
        String description,

        @NotNull
        @Schema(description = "The Maven coordinates of the artifacts to load.")
        List<String> coordinates) { }

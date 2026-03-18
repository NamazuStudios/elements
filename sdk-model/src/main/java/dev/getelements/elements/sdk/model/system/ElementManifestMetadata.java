package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.SystemVersion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Represents the Element manifest metadata.
 *
 * @param version the version of the Element
 * @param builtinSpis the builtin SPIs requested by this element
 */
@Schema(description = "The Element manifest.")
public record ElementManifestMetadata(
        @Schema(description = "The version of the Element")
        SystemVersion version,

        @Schema(description = "The builtin SPIs requested by this element.")
        List<String> builtinSpis
) {}

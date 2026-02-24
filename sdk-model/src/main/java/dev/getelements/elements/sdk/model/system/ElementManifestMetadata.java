package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.SystemVersion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "The Element manifest.")
public record ElementManifestMetadata(
        @Schema(description = "The version of the Element")
        SystemVersion version,

        @Schema(description = "The builtin SPIs requested by this element.")
        List<String> builtinSpis
) {}

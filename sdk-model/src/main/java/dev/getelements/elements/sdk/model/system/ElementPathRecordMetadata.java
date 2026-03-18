package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.record.ElementManifestRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * DTO for {@link dev.getelements.elements.sdk.record.ElementPathRecord}.
 *
 * @param path the path inside the ELM file
 * @param api the API paths inside the ELM file
 * @param spi the SPI paths inside the ELM file
 * @param lib the library paths inside the ELM file
 * @param classpath the classpath paths inside the ELM file
 * @param attributes the attributes specified in the ELM file
 * @param manifest the manifest metadata
 */
@Schema(description = "Describes the path of an ELM distribution.")
public record ElementPathRecordMetadata(
        @Schema(description = "The path inside the ELM file.")
        String path,

        @Schema(description = "The API path inside the ELM file.")
        List<String> api,

        @Schema(description = "The SPI path inside the ELM file.")
        List<String> spi,

        @Schema(description = "The library path inside the ELM file.")
        List<String> lib,

        @Schema(description = "The classpath path inside the ELM file.")
        List<String> classpath,

        @Schema(description = "The the attributes specified in the ELM file.")
        Map<String, Object> attributes,

        @Schema(description = "The manifest reo")
        ElementManifestMetadata manifest
) {}

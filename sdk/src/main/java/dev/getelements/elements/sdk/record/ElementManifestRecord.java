package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.SystemVersion;

import java.util.Arrays;
import java.util.List;

/**
 * Parsed content of the Element manifest properties file ({@code dev.getelements.element.manifest.properties}).
 * Element authors place this file at the root of their element directory to declare version metadata and the names
 * of builtin SPIs the element requires. All fields are optional; absent entries default to
 * {@link SystemVersion#UNKNOWN}.
 *
 * <p>Example manifest file:</p>
 * <pre>{@code
 * Element-Version=1.0.0
 * Element-Build-Time=2026-01-15T10:30:00Z
 * Element-Revision=abc123def456
 * Element-Builtin-Spis=DEFAULT
 * }</pre>
 *
 * <p>The three version-related properties ({@value #ELEMENT_VERSION}, {@value #ELEMENT_REVISION},
 * {@value #ELEMENT_BUILD_TIME}) are mapped to the {@link SystemVersion} record fields
 * {@code version}, {@code revision}, and {@code timestamp} respectively.</p>
 *
 * @param version     the element's {@link SystemVersion}; defaults to {@link SystemVersion#UNKNOWN} when absent
 * @param builtinSpis names of {@code BuiltinSpi} enum values this element requires; never {@code null}
 */
public record ElementManifestRecord(
        SystemVersion version,
        List<String> builtinSpis
) {

    /**
     * Attribute key for the element version string, mapped to {@link SystemVersion#version()}.
     */
    public static final String ELEMENT_VERSION = "Element-Version";

    /**
     * Attribute key for the element source revision, mapped to {@link SystemVersion#revision()}.
     */
    public static final String ELEMENT_REVISION = "Element-Revision";

    /**
     * Attribute key for the ISO-8601 build timestamp, mapped to {@link SystemVersion#timestamp()}.
     */
    public static final String ELEMENT_BUILD_TIME = "Element-Build-Time";

    /**
     * Attribute key for the comma-separated list of builtin SPI names.
     */
    public static final String ELEMENT_BUILTIN_SPIS = "Element-Builtin-Spis";

    public ElementManifestRecord {
        version = version == null ? SystemVersion.UNKNOWN : version;
        builtinSpis = builtinSpis == null ? List.of() : List.copyOf(builtinSpis);
    }

    /**
     * Parses an {@link ElementManifestRecord} from an {@link Attributes} instance such as one returned by
     * {@link dev.getelements.elements.sdk.ElementPathLoader#readManifest}. Any absent version field falls back to
     * the corresponding field of {@link SystemVersion#UNKNOWN}.
     *
     * @param attributes the attributes to parse
     * @return the parsed record
     */
    public static ElementManifestRecord from(final Attributes attributes) {

        final var v = (String) attributes.getAttribute(ELEMENT_VERSION);
        final var r = (String) attributes.getAttribute(ELEMENT_REVISION);
        final var t = (String) attributes.getAttribute(ELEMENT_BUILD_TIME);

        final var systemVersion = new SystemVersion(
                v != null ? v : SystemVersion.UNKNOWN.version(),
                r != null ? r : SystemVersion.UNKNOWN.revision(),
                t != null ? t : SystemVersion.UNKNOWN.timestamp()
        );

        final var raw = (String) attributes.getAttribute(ELEMENT_BUILTIN_SPIS);

        final var spis = (raw == null || raw.isBlank())
                ? List.<String>of()
                : Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

        return new ElementManifestRecord(systemVersion, spis);

    }

    /**
     * Returns an empty manifest with {@link SystemVersion#UNKNOWN} and no builtin SPIs.
     *
     * @return an empty {@link ElementManifestRecord}
     */
    public static ElementManifestRecord empty() {
        return new ElementManifestRecord(SystemVersion.UNKNOWN, List.of());
    }

}
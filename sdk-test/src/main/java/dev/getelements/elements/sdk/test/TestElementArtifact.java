package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.ElementPathLoader;

import java.util.Map;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.ElementPathLoader.ELM_EXTENSION;

/**
 * Represents a specific test artifact in the collection of test assets.
 */
public enum TestElementArtifact {

    API(
            "dev.getelements.elements:sdk-test-api:%s",
            "dev.getelements.elements:sdk-test-api:elm:%s",
            "dev.getelements.elements.sdk.test.element"
    ),
    BASE(
            "dev.getelements.elements:sdk-test-element:%s",
            "dev.getelements.elements:sdk-test-element:elm:%s",
            "dev.getelements.elements.sdk.test.element"
    ),
    VARIANT_A(
            "dev.getelements.elements:sdk-test-element-a:%s",
            "dev.getelements.elements:sdk-test-element-a:elm:%s",
            "dev.getelements.elements.sdk.test.element.a"
    ),
    VARIANT_B(
            "dev.getelements.elements:sdk-test-element-b:%s",
            "dev.getelements.elements:sdk-test-element-b:elm:%s",
            "dev.getelements.elements.sdk.test.element.b"
    ),
    JAKARTA_RS(
            "dev.getelements.elements:sdk-test-element-rs:%s",
            "dev.getelements.elements:sdk-test-element-rs:elm:%s",
            "dev.getelements.elements.sdk.test.element.rs"
    ),
    JAKARTA_WS(
            "dev.getelements.elements:sdk-test-element-ws:%s",
            "dev.getelements.elements:sdk-test-element-ws:elm:%s",
            "dev.getelements.elements.sdk.test.element.ws"
    );

    private final String coordinates;

    private final String coordinatesForElm;

    private final String elementName;

    private final Map<?, ?> attributes = Map.of("dev.getelements.test.variant", toString());

    TestElementArtifact(
            final String coordinates,
            final String coordinatesForElm,
            final String elementName) {
        // Ensures validity of format string without checking the system define
        coordinates.formatted("");
        this.coordinates = coordinates;
        this.coordinatesForElm = coordinatesForElm;
        this.elementName = elementName;
    }

    /**
     * Gets the singular coordinates for the artifact.
     *
     * @return the singular coordinates for the artifact
     */
    public String getCoordinates() {

        final var version = System.getProperty("maven.version");

        if (version == null) {
            throw new IllegalStateException("`maven.version` property is null. This test requires a Maven project");
        }

        return coordinates.formatted(version);

    }

    /**
     * Gets the coordinates for the artifact with the elm classifier.
     *
     * @return the elm-qualified coordinates
     */
    public String getCoordinatesForElm() {

        final var version = System.getProperty("maven.version");

        if (version == null) {
            throw new IllegalStateException("`maven.version` property is null. This test requires a Maven project");
        }

        return coordinatesForElm.formatted(version);
    }

    public Stream<String> getAllCoordinates() {
        return Stream.of(getCoordinates());
    }

    public String getElementName() {
        return elementName;
    }

    public Map<?, ?> getAttributes() {
        return attributes;
    }

}

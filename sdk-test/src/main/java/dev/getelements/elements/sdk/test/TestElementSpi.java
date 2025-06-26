package dev.getelements.elements.sdk.test;

import java.util.List;

/**
 * Enumerates the SPI (Service Provider Interface) types used in testing elements. Each SPI type corresponds to a set of
 * artifacts that can be loaded for testing purposes.
 */
public enum TestElementSpi {

    /**
     * The base SPI type, which includes the core SDK SPI artifact.
     */
    BASE("sdk-spi"),

    /**
     * The SPI type that uses Guice for dependency injection.
     */
    GUICE("sdk-spi", "sdk-spi-guice");

    private final List<String> artifacts;

    TestElementSpi(final String ... artifacts) {
        this.artifacts = List.of(artifacts);
    }

    /**
     * Gets all the artifacts associated with this SPI type.
     *
     * @return the artifacts
     */
    public List<String> getArtifacts() {
        return artifacts;
    }

}

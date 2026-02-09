package dev.getelements.elements.sdk.test;

import java.util.stream.Stream;

/**
 * Enumerates the SPI (Service Provider Interface) types used in testing elements. Each SPI type corresponds to a set of
 * artifacts that can be loaded for testing purposes.
 */
public enum TestElementSpi {

    /**
     * The SPI type that uses Guice for dependency injection.
     */
    GUICE_7_0_X(
            "dev.getelements.elements:sdk-spi-guice:%s",
            "com.google.guice:guice:7.0.0",
            "com.google.guava:guava:33.1.0-jre"
    );

    private final String spiCoordinates;

    private final String[] supportingCoordinates;

    TestElementSpi(final String spiCoordinates,
                   final String ... supportingCoordinates) {
        // Ensures validity of format string without checking the system define
        spiCoordinates.formatted("");
        this.spiCoordinates = spiCoordinates;
        this.supportingCoordinates = supportingCoordinates;
    }

    /**
     * Gets the fully qualified coordinates for this SPI.
     * @return the coordinates
     */
    public Stream<String> getSpiCoordinates() {

        final var version = System.getProperty("maven.version");

        if (version == null) {
            throw new IllegalStateException("`maven.version` property is null. This test requires a Maven project");
        }

        return Stream.concat(
                Stream.of(spiCoordinates.formatted(version)),
                Stream.of(supportingCoordinates)
        );

    }

}

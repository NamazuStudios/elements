package dev.getelements.elements.sdk.test;

import java.nio.file.Path;

/**
 * Enumerates the SPI (Service Provider Interface) types used in testing elements. Each SPI type corresponds to a set of
 * artifacts that can be loaded for testing purposes.
 */
public enum TestElementSpi {

    /**
     * The base SPI type, which includes the core SDK SPI artifact.
     */
    BASE,

    /**
     * The SPI type that uses Guice for dependency injection.
     */
    GUICE_7_0_X;

    private final Path base = Path.of("SPI", name());

    /**
     * Gets the base path for this SPI type. This is the directory where the artifacts for this SPI type are located.
     * @return the base path
     */
    public Path getBase() {
        return base;
    }

}

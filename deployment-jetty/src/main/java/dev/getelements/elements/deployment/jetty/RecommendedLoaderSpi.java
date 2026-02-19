package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.model.system.ElementSpi;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.SystemVersion.CURRENT;

/**
 * The recommended loader SPIs based on the current system version and configurations.
 */
public enum RecommendedLoaderSpi {

    DEFAULT(
            CURRENT.version(),
            "The default loader.",
            "dev.getelements.elements:sdk-spi-guice:%s".formatted(CURRENT.version()),
            "com.google.guice:guice:7.0.0",
            "com.google.guava:guava:33.1.0-jre",
            "aopalliance:aopalliance:1.0"
    ),
    GUICE_7_0_0(
            CURRENT.version(),
            "Guice 7.0.0 loader with default Guava version.",
            "dev.getelements.elements:sdk-spi-guice:%s".formatted(CURRENT.version()),
            "com.google.guice:guice:7.0.0",
            "com.google.guava:guava:33.1.0-jre",
            "aopalliance:aopalliance:1.0"
    ),
    GUICE_7_0_0_NO_GUAVA(
            CURRENT.version(),
            "Guice 7.0.0 loader without Guava (must specify your own compatible with Guice 7.0.0.",
            "dev.getelements.elements:sdk-spi-guice:%s".formatted(CURRENT.version()),
            "com.google.guice:guice:7.0.0",
            "aopalliance:aopalliance:1.0"
    ),
    GUICE_7_0_0_GUAVA_33_1_0(
            CURRENT.version(),
            "Guice 7.0.0 loader with Guava version 33.1.0.",
            "dev.getelements.elements:sdk-spi-guice:%s".formatted(CURRENT.version()),
            "com.google.guice:guice:7.0.0",
            "com.google.guava:guava:33.1.0-jre",
            "aopalliance:aopalliance:1.0"
    );

    private final String version;

    private final String description;

    private final List<String> coordinates;

    RecommendedLoaderSpi(final String version,
                         final String description,
                         final String ... coordinates) {
        this.version = version;
        this.description = description;
        this.coordinates = List.of(coordinates);
    }

    /**
     * Attempts to find the recommended loader SPI for the supplied identifier.
     *
     * @param id the identifier
     * @return an {@link Optional} containing the SPI
     */
    public static Optional<RecommendedLoaderSpi> findRecommendedLoaderSpi(final String id) {
        return Stream.of(values()).filter(spi -> spi.version.equals(id)).findFirst();
    }

    /**
     * Converts to the {@link ElementSpi} DTO.
     *
     * @return the {@link ElementSpi}
     */
    public ElementSpi toElementSpi() {
        return new ElementSpi(toString(), version, description, coordinates);
    }

}

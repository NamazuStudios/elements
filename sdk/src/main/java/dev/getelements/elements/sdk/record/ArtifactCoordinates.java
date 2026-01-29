package dev.getelements.elements.sdk.record;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A record representing artifact coordinates.
 *
 * @param coordinates the actual coordinates, the actual artifact coordinates.
 * @param repositories the repositories from which to search for the requested coordinates
 */
public record ArtifactCoordinates(String coordinates, Set<ArtifactRepository> repositories) {

    /**
     * Pre-checks all parameters for validity.
     *
     * @param coordinates the coordinates
     * @param repositories the repositories
     */
    public ArtifactCoordinates {

        coordinates = requireNonNull(coordinates, "coordinates");
        repositories = requireNonNull(repositories, "repositories");

        if (repositories.isEmpty()) {
            throw new IllegalArgumentException("repositories cannot be empty");
        }

    }

    /**
     * Builds a {@link ArtifactCoordinates} record from coordinates.
     * @param coordinates the coordinates
     * @param first the first repository
     * @param subsequent subsequent
     * @return a new {@link ArtifactCoordinates}
     */
    public static ArtifactCoordinates fromCoordinates(
            final String coordinates,
            final ArtifactRepository first,
            final ArtifactRepository ... subsequent) {

        final var repositories = Stream
                .concat(Stream.of(first), Stream.of(subsequent))
                .collect(Collectors.toUnmodifiableSet());

        return new ArtifactCoordinates(coordinates, repositories);

    }

    /**
     * Specifies the default {@link ArtifactRepository}, adding any additional subsequent repositories.
     *
     * @param coordinates the coordinates
     * @param subsequent the subsequent artifact repositories
     * @return a new {@link ArtifactCoordinates}
     */
    public static ArtifactCoordinates fromCoordinatesAndDefaultRepository(
            final String coordinates,
            final ArtifactRepository ... subsequent) {
        return fromCoordinates(coordinates, ArtifactRepository.DEFAULT, subsequent);
    }

}

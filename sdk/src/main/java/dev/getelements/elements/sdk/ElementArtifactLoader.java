package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkArtifactNotFoundException;
import dev.getelements.elements.sdk.record.ArtifactCoordinates;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An interface to load artifacts from coordinates. Artifacts are bundles of code, typically in jar form, that can
 * provide additional functionality. This service can load artifacts on-the-fly from various sources and load them into
 * the application.
 */
public interface ElementArtifactLoader {

    /**
     * Gets the {@link ClassLoader} loaded with the supplied coordinates.
     *
     * @param parent the parent {@link ClassLoader}, may be null indicating that the system classpath is to be used
     * @param first the first coordinates
     * @param additional the additional coordinates
     * @return the {@link ClassLoader}
     */
    default ClassLoader getClassLoader(final ClassLoader parent,
                                       final ArtifactCoordinates first,
                                       final ArtifactCoordinates ... additional) {
        return tryGetClassLoader(parent, first, additional).orElseThrow(SdkArtifactNotFoundException::new);
    }

    /**
     * Opens a {@link ClassLoader} with the supplied coordinates. This will resolve all requested coordinates and then
     * return a {@link ClassLoader} with the artifacts.
     *
     * @return a {@link ClassLoader} for the artifact registry
     */
    default ClassLoader getClassLoader(final ClassLoader parent, final Set<ArtifactCoordinates> coordinates) {
        return tryGetClassLoader(parent, coordinates).orElseThrow(SdkArtifactNotFoundException::new);
    }

    /**
     * Gets the {@link ClassLoader} loaded with the supplied coordinates.
     *
     * @param parent the parent {@link ClassLoader}, may be null indicating that the system classpath is to be used
     * @param first the first coordinates
     * @param additional the additional coordinates
     * @return the {@link ClassLoader}
     */
    default Optional<ClassLoader> tryGetClassLoader(final ClassLoader parent,
                                                    final ArtifactCoordinates first,
                                                    final ArtifactCoordinates ... additional) {

        final var coordinatesSet = Stream
                .concat(Stream.of(first), Stream.of(additional))
                .collect(Collectors.toUnmodifiableSet());

        return tryGetClassLoader(parent, coordinatesSet);

    }

    /**
     * Opens a {@link ClassLoader} with the supplied coordinates. This will resolve all requested coordinates and then
     * return a {@link ClassLoader} with the artifacts.
     *
     * @return a {@link ClassLoader} for the artifact registry
     */
    Optional<ClassLoader> tryGetClassLoader(ClassLoader parent, Set<ArtifactCoordinates> coordinates);

}

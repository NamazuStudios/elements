package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkArtifactNotFoundException;
import dev.getelements.elements.sdk.record.ArtifactRepository;

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
     * @param repositories the set of repositories to use for resolution
     * @param coordinates a single set of coordinates
     * @return the {@link ClassLoader}
     */
    default ClassLoader getClassLoader(final ClassLoader parent,
                                       final Set<ArtifactRepository> repositories,
                                       final String coordinates) {
        return tryGetClassLoader(
                parent,
                repositories,
                coordinates,
                new String[0]
        ).orElseThrow(SdkArtifactNotFoundException::new);
    }

    /**
     * Gets the {@link ClassLoader} loaded with the supplied coordinates.
     *
     * @param parent the parent {@link ClassLoader}, may be null indicating that the system classpath is to be used
     * @param repositories the set of repositories to use for resolution
     * @param first the first coordinates
     * @param additional the additional coordinates
     * @return the {@link ClassLoader}
     */
    default ClassLoader getClassLoader(final ClassLoader parent,
                                       final Set<ArtifactRepository> repositories,
                                       final String first,
                                       final String ... additional) {
        return tryGetClassLoader(
                parent,
                repositories,
                first,
                additional
        ).orElseThrow(SdkArtifactNotFoundException::new);
    }

    /**
     * Opens a {@link ClassLoader} with the supplied coordinates. This will resolve all requested coordinates and then
     * return a {@link ClassLoader} with the artifacts.
     *
     * @param parent the parent {@link ClassLoader}, may be null indicating that the system classpath is to be used
     * @param repositories the set of repositories to use for resolution
     * @param coordinates the full set of coordinates
     * @return a {@link ClassLoader} for the artifact registry
     */
    default ClassLoader getClassLoader(final ClassLoader parent,
                                       final Set<ArtifactRepository> repositories,
                                       final Set<String> coordinates) {
        return tryGetClassLoader(
                parent,
                repositories,
                coordinates
        ).orElseThrow(SdkArtifactNotFoundException::new);
    }

    /**
     * Gets the {@link ClassLoader} loaded with the supplied coordinates.
     *
     * @param parent the parent {@link ClassLoader}, may be null indicating that the system classpath is to be used
     * @param repositories the set of repositories to use for resolution
     * @param first the first coordinates
     * @param additional the additional coordinates
     *
     * @return the {@link ClassLoader}
     */
    default Optional<ClassLoader> tryGetClassLoader(final ClassLoader parent,
                                                    final Set<ArtifactRepository> repositories,
                                                    final String first,
                                                    final String ... additional) {

        final var coordinatesSet = Stream
                .concat(Stream.of(first), Stream.of(additional))
                .collect(Collectors.toUnmodifiableSet());

        return tryGetClassLoader(parent, repositories, coordinatesSet);

    }

    /**
     * Opens a {@link ClassLoader} with the supplied coordinates. This will resolve all requested coordinates and then
     * return a {@link ClassLoader} with the artifacts.
     *
     * @return a {@link ClassLoader} for the artifact registry
     */
    Optional<ClassLoader> tryGetClassLoader(ClassLoader parent,
                                            Set<ArtifactRepository> repositories,
                                            Set<String> coordinates);

}

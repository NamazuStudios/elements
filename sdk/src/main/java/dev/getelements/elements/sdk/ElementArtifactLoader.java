package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkArtifactNotFoundException;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.Artifact;
import dev.getelements.elements.sdk.record.ArtifactRepository;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;

/**
 * An interface to load artifacts from coordinates. Artifacts are bundles of code, typically in jar form, that can
 * provide additional functionality. This service can load artifacts on-the-fly from various sources and load them into
 * the application.
 *
 * Unless otherwise specified, the find family of methods will throw exceptions for any reason except a missing
 * artifact.
 */
public interface ElementArtifactLoader {

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
        return findClassLoader(
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
     * @throws SdkArtifactNotFoundException if one or more of the requested artifacts can't be found
     */
    default ClassLoader getClassLoader(final ClassLoader parent,
                                       final Set<ArtifactRepository> repositories,
                                       final Set<String> coordinates) {
        return findClassLoader(
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
    default Optional<ClassLoader> findClassLoader(final ClassLoader parent,
                                                  final Set<ArtifactRepository> repositories,
                                                  final String first,
                                                  final String ... additional) {

        final var coordinatesSet = Stream
                .concat(Stream.of(first), Stream.of(additional))
                .collect(Collectors.toUnmodifiableSet());

        return findClassLoader(parent, repositories, coordinatesSet);

    }

    /**
     * Opens a {@link ClassLoader} with the supplied coordinates. This will resolve all requested coordinates and then
     * return a {@link ClassLoader} with the artifacts.
     *
     * @return a {@link ClassLoader} for the artifact registry
     */
    Optional<ClassLoader> findClassLoader(ClassLoader parent,
                                          Set<ArtifactRepository> repositories,
                                          Set<String> coordinates);

    /**
     * Gets a single artifact.
     *
     * @param repositories the repositories to search
     * @param coordinates the coordinates
     * @return the {@link Artifact}
     * @throws SdkArtifactNotFoundException if the artifact can't be found
     */
    default Artifact getArtifact(final Set<ArtifactRepository> repositories,
                                  final String coordinates) {
        return findArtifact(repositories, coordinates).orElseThrow(SdkArtifactNotFoundException::new);
    }

    /**
     * Finds the classpath for a particular artifact, including all transient dependencies.
     *
     * @param repositories the repositories to search
     * @param coordinates the coordinates
     *
     * @return a stream of {@link Artifact}s
     */
    Stream<Artifact> findClasspathForArtifact(Set<ArtifactRepository> repositories, String coordinates);

    /**
     * Finds the artifact with the repositories and coordinates.
     *
     * @param repositories the repositories to search
     * @param coordinates the coordinates
     *
     * @return an {@link Optional} with the artifact
     */
    Optional<Artifact> findArtifact(Set<ArtifactRepository> repositories, String coordinates);

    /**
     * Gets a new default instance of the {@link ElementArtifactLoader}.
     *
     * @return the {@link ElementArtifactLoader}
     */
    static ElementArtifactLoader newDefaultInstance() {
        return ServiceLoader.load(ElementArtifactLoader.class, currentThread().getContextClassLoader())
                .stream()
                .findFirst()
                .orElseThrow(() -> new SdkException("No ElementArtifactLoader SPI Found"))
                .get();
    }

}

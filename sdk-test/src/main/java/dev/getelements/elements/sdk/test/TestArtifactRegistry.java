package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.Artifact;
import dev.getelements.elements.sdk.record.ArtifactRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static java.nio.file.Files.isDirectory;

/**
 * A helper class to find the test artifacts in the project. Not intended to be used outside the SDK integration
 * tests. This uses the {@link ElementArtifactLoader} (introduced in 3.7) to load test artifacts from the local
 * Maven repository.
 */
public class TestArtifactRegistry {

    private final ElementArtifactLoader elementArtifactLoader = ElementArtifactLoader.newDefaultInstance();

    /**
     * Finds the SPI of the artifact with the artifact name.
     *
     * @param spi the SPI
     * @return the URL of the artifact
     * @throws java.util.NoSuchElementException if the artifact wasn't found
     */
    public Stream<URL> findSpiUrls(final TestElementSpi spi) {
        return findSpiPaths(spi)
                .map(p -> {
                    try {
                        return p.toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new SdkException(e);
                    }
                });
    }

    /**
     * Finds the SPI of the artifact with the artifact name.
     *
     * @param spi the SPI
     * @return the URL of the artifact
     */
    public Stream<Path> findSpiPaths(final TestElementSpi spi) {

        final var repositories = ArtifactRepository.DEFAULTS;

        return spi.getSpiCoordinates()
                .flatMap(c -> elementArtifactLoader.findClasspathForArtifact(repositories, c))
                .map(Artifact::path);

    }

    /**
     * Finds the URL of the artifact with the artifact name.
     *
     * @param artifact the artifact name
     * @return the URL of the artifact
     * @throws java.util.NoSuchElementException if the artifact wasn't found
     */
    public URL findArtifactUrl(final TestElementArtifact artifact) {
        try {
            return findArtifactPath(artifact).toUri().toURL();
        } catch (MalformedURLException e) {
            throw new SdkException(e);
        }
    }

    /**
     * Finds the Path of the artifact with the artifact name.
     *
     * @param artifact the artifact
     * @return the Path of the artifact
     * @throws java.util.NoSuchElementException if the artifact wasn't found
     */
    public Path findArtifactPath(final TestElementArtifact artifact) {

        final var coordinates = artifact.getCoordinates();

        return elementArtifactLoader
                .findArtifact(ArtifactRepository.DEFAULTS, coordinates)
                .map(Artifact::path)
                .orElseThrow(NoSuchElementException::new);

    }

    /**
     * Copies the specified artifact to the path. If the destination path is a directory it will be copied into
     * that directory. Otherwise, it will be copied to the destination exactly as it is.
     *
     * @param spi the spi
     * @param destination the artifact destination
     * @throws java.util.NoSuchElementException if the artifact wasn't found
     */
    public void copySpiTo(final TestElementSpi spi, final Path destination) throws IOException {
        for (final var source : findSpiPaths(spi).toList()) {
            Files.copy(source, isDirectory(destination)
                    ? destination.resolve(source.getFileName())
                    : destination
            );
        }
    }

    /**
     * Copies the specified artifact to the path. If the destination path is a directory it will be copied into
     * that directory. Otherwise, it will be copied to the destination exactly as it is.
     *
     * @param artifact the artifact name
     * @param destination the artifact destination
     * @throws java.util.NoSuchElementException if the artifact wasn't found
     */
    public void copyArtifactTo(final TestElementArtifact artifact, final Path destination) throws IOException {

        final var source = findArtifactPath(artifact);

        Files.copy(source, isDirectory(destination)
                ? destination.resolve(source.getFileName())
                : destination
        );

    }

    /**
     * Unpacks the artifact into the destination directory.
     *
     * @param artifact the artifact name
     * @param destinationDirectory the destination directory
     */
    public void unpackArtifact(final TestElementArtifact artifact, final Path destinationDirectory) {

        if (!isDirectory(destinationDirectory)) {
            throw new IllegalArgumentException(destinationDirectory + " is not a directory");
        }

        final var jarFilePath = findArtifactPath(artifact);

        try (var jarFile = new JarFile(jarFilePath.toFile())) {

            final var jarFileIterator = jarFile
                    .stream()
                    .filter(Predicate.not(JarEntry::isDirectory))
                    .iterator();

            while (jarFileIterator.hasNext()) {
                final var jarEntry = jarFileIterator.next();
                final var jarEntryPath = destinationDirectory.resolve(jarEntry.getName());
                Files.createDirectories(jarEntryPath.getParent());

                try (var is = jarFile.getInputStream(jarEntry);
                     var os = new FileOutputStream(jarEntryPath.toFile())) {
                    is.transferTo(os);
                }

            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

}

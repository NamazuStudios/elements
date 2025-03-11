package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.exception.SdkException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.nio.file.Files.isDirectory;

/**
 * A helper class to find the test artifacts in the project. Not intended to be used outside the SDK integration
 * tests. This searches the current working directory for "test-element-artifacts" and works it way upward until
 * it can find a set of test artifacts.
 */
public class TestArtifactRegistry {

    private static final String JAR_EXTENSION = ".jar";

    private static final String TEST_ELEMENT_JARS = "test-element-artifacts";

    private static final Path artifactRoot = determineArtifactRoot();

    private static Path determineArtifactRoot() {

        var dir = Path.of(".").toAbsolutePath().normalize();

        do {

            final var artifactPath = dir.resolve(TEST_ELEMENT_JARS);

            if (isDirectory(artifactPath)) {
                return artifactPath;
            } else {
                dir = dir.getParent();
            }

        } while (dir != null);

        return null;

    }

    public TestArtifactRegistry() {
        if (artifactRoot == null) {
            throw new IllegalStateException(
                    "Unable to determine artifact root from: " +
                    Path.of(".").toAbsolutePath()
            );
        }
    }

    /**
     * Finds the URL of the artifact with the artifact name.
     *
     * @param artifact the artifact name
     * @return the URL of the artifact
     * @throws java.util.NoSuchElementException if the artifact wasn't found
     */
    public URL findJarUrl(final TestElementArtifact artifact) {
        try {
            return findArtifactPath(artifact).toUri().toURL();
        } catch (MalformedURLException ex) {
            throw new SdkException(ex);
        }
    }

    /**
     * Finds the Path of the bundle with the bundle name.
     *
     * @param bundle the artifact
     * @return the Path of the artifact
     * @throws java.util.NoSuchElementException if the artifact wasn't found
     */
    public Path findBundlePath(final TestElementBundle bundle) {
        try {
            return Files.list(artifactRoot)
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().equals(bundle.getDirectoryName()))
                    .findFirst()
                    .get();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
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
        try {
            return Files.list(artifactRoot)
                    .filter(p -> p.getFileName().toString().endsWith(JAR_EXTENSION))
                    .filter(p -> p.getFileName().toString().startsWith(artifact.getArtifact()))
                    .findFirst()
                    .get();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Copies all artifacts in the bundle to the specified directory.
     *
     * @param bundle the bundle
     * @param destination the bundle destination
     * @throws java.util.NoSuchElementException if the artifact wasn't found
     */
    public void copyBundleTo(final TestElementBundle bundle,
                             final Path destination) {

        if (!isDirectory(destination)) {
            throw new IllegalArgumentException(destination + " is not a directory");
        }

        final var sourceDirectory = findBundlePath(bundle);
        try {
            Files.list(sourceDirectory)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(JAR_EXTENSION))
                    .forEach(p -> {
                        try {
                            Files.copy(p, destination.resolve(p.getFileName()));
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

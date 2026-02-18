package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static java.nio.file.Files.copy;
import static java.util.Objects.requireNonNull;

/**
 * Encapsulates the mutable context used during element deployment operations.
 * This record groups together the registry and various tracking lists that are
 * passed through the deployment pipeline.
 */
record DeploymentContext(
        ElementDeployment deployment,
        MutableElementRegistry registry,
        List<Path> tempFiles,
        List<FileSystem> fileSystems,
        List<String> logs,
        List<String> warnings,
        List<Throwable> errors,
        List<Path> elementPaths,
        Map<Path, List<String>> spiPaths,
        Map<Path, Attributes> attributePaths,
        ElementArtifactLoader artifactLoader,
        Set<ArtifactRepository> repositories,
        TemporaryFiles temporaryFiles
) {

    public DeploymentContext {
        requireNonNull(deployment, "deployment");
        repositories = repositories == null ? new HashSet<>() : repositories;
        buildRepositories(deployment, repositories);
    }

    public static DeploymentContext create(
            final ElementDeployment deployment,
            final MutableElementRegistry registry,
            final ElementArtifactLoader artifactLoader,
            final TemporaryFiles temporaryFiles
    ) {
        return new DeploymentContext(
                deployment,
                registry,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                artifactLoader,
                new HashSet<>(),
                temporaryFiles
        );
    }

    private static void buildRepositories(
            final ElementDeployment deployment,
            final Set<ArtifactRepository> repositories) {

        // Add default repositories if requested
        if (deployment.useDefaultRepositories()) {
            repositories.addAll(ArtifactRepository.DEFAULTS);
        }

        // Add explicit repositories
        if (deployment.repositories() != null) {
            deployment.repositories()
                    .stream()
                    .map(ear -> new ArtifactRepository(ear.id(), ear.url()))
                    .forEach(repositories::add);
        }

    }

    /**
     * Creates a custom SPI classloader for the given element path if custom SPI dependencies are configured.
     * If no custom SPI is configured, returns the parent classloader.
     *
     * @param parent           the parent classloader
     * @param elementPath      the element path to create SPI classloader for
     * @return a classloader with the SPI dependencies, or the parent if no custom SPI
     * @throws IOException if artifact resolution or file operations fail
     */
    public ClassLoader createSpiClassLoaderFor(final ClassLoader parent, final Path elementPath) {

        final var spiClassPath = spiPaths.get(elementPath);

        if (spiClassPath == null) {
            logs.add("%s uses default SPI. Not loading SPI".formatted(elementPath));
            return parent;
        }

        final var spiTarget = temporaryFiles.createTempDirectory("spi");
        tempFiles.add(spiTarget);

        for (final var coordinates : spiClassPath) {
            try {
                copyArtifactWithDependencies(coordinates, spiTarget);
            } catch (IOException e) {
                warnings.add(
                        "Caught IO Exception assembling classpath %s"
                                .formatted(e.getMessage())
                );
                errors.add(e);
            }
        }

        // Collect all JAR files in the spiTarget directory for the classloader
        URL[] jarUrls;

        try (final var pathStream = Files.walk(spiTarget, 1)) {
            jarUrls = pathStream
                    .filter(p -> p.toString().endsWith(".jar"))
                    .map(p -> {
                        try {
                            return p.toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(URL[]::new);
        } catch (IOException e) {
            jarUrls = new URL[0];
            warnings.add("Caught IO Exception assembling classpath %s".formatted(e.getMessage()));
            errors.add(e);
        }

        if (jarUrls.length == 0) {
            warnings.add("No JAR files found in SPI directory for path: " + elementPath);
        }

        return new URLClassLoader(jarUrls, parent);

    }

    /**
     * Copies a Maven artifact and its transitive dependencies to a target directory.
     * Logs the resolution and copying process to the deployment logs.
     *
     * @param coordinates the Maven coordinates (e.g., "groupId:artifactId:version")
     * @param targetDir   the target directory to copy artifacts into
     * @throws IOException if artifact resolution or file copying fails
     */
    public void copyArtifactWithDependencies(
            final String coordinates,
            final Path targetDir
    ) throws IOException {

        logs.add("Resolving artifact with dependencies: " + coordinates);

        final var artifacts = artifactLoader.findClasspathForArtifact(repositories, coordinates).toList();
        logs.add("Found %d artifact(s) including dependencies %s".formatted(artifacts.size(), coordinates));

        for (final var artifact : artifacts) {

            final var sourcePath = artifact.path();
            final var fileName = "%s.%s.%s.%s".formatted(
                    artifact.group(),
                    artifact.id(),
                    artifact.version(),
                    artifact.extension()
            );

            final var destinationPath = targetDir.resolve(fileName);
            copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            logs.add("Copied artifact: %s".formatted(fileName));
            tempFiles.add(destinationPath);

        }

    }

    public Attributes createAttributesForPath(final Attributes baseAttrs, final Path elementPath) {

        // Look up pre-computed attributes for this path
        final var resolved = attributePaths().getOrDefault(
                elementPath.toAbsolutePath(),
                Attributes.emptyAttributes()
        );

        // Create the base attributes found in the deployment
        return new SimpleAttributes.Builder()
                .from(baseAttrs)
                .from(resolved)
                .build();

    }

    /**
     * Creates a new element deployment directory capable of serving multiple elements.
     *
     * @return the deployment directory
     */
    public Path createDeploymentDirectory() {
        final var deploymentDir = temporaryFiles.createTempDirectory("deployment-%s-".formatted(deployment.id()));
        elementPaths().add(deploymentDir);
        return deploymentDir;
    }

    /**
     * Creates a temporary file with prefix and suffix. Logs the file to the {@link DeploymentContext#tempFiles()} such
     * that it can be reported and later cleaned up.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the {@link Path}
     */
    public Path createTempFile(final String prefix, final String suffix) {
        final var file = temporaryFiles.createTempFile(prefix, suffix);
        tempFiles.add(file);
        return file;
    }

}

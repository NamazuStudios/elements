package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.SystemVersion;
import dev.getelements.elements.sdk.record.ElementManifestRecord;
import dev.getelements.elements.sdk.record.ElementPathRecord;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import dev.getelements.elements.sdk.util.TemporaryFiles;

import java.io.IOException;
import java.nio.file.FileSystem;
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
        List<Path> deploymentFiles,
        List<FileSystem> fileSystems,
        List<String> logs,
        List<String> warnings,
        List<Throwable> errors,
        List<Path> elementPaths,
        Map<Path, Set<String>> spiPaths,
        Map<Path, Attributes> attributePaths,
        Map<Path, ElementManifestRecord> manifests,
        Map<Element, ElementPathRecord> elementPathsByElement,
        ElementArtifactLoader artifactLoader,
        ElementPathLoader pathLoader,
        Set<ArtifactRepository> repositories,
        TemporaryFiles temporaryFiles,
        Set<Path> unconsumedSpiPaths,
        Set<Path> unconsumedAttributePaths
) {

    public DeploymentContext {
        requireNonNull(deployment, "deployment");
        requireNonNull(pathLoader, "pathLoader");
        repositories = repositories == null ? new HashSet<>() : repositories;
        buildRepositories(deployment, repositories);
    }

    public static DeploymentContext create(
            final ElementDeployment deployment,
            final MutableElementRegistry registry,
            final ElementArtifactLoader artifactLoader,
            final ElementPathLoader pathLoader,
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
                new HashMap<>(),
                new LinkedHashMap<>(),
                artifactLoader,
                pathLoader,
                new HashSet<>(),
                temporaryFiles,
                new HashSet<>(),
                new HashSet<>()
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
     * Appends an INFO-level message to the deployment log.
     *
     * @param message the message to log
     */
    public void log(final String message) {
        logs.add("INFO: %s".formatted(message));
    }

    /**
     * Appends a WARN-level message to the warnings list and to the deployment log.
     *
     * @param message the message to log
     */
    public void warn(final String message) {
        logs.add("WARN: %s".formatted(message));
        warnings.add(message);
    }

    /**
     * Appends an error to the errors list and the deployment log.
     * @param th the error
     */
    public void error(final Throwable th) {
        logs.add("WARN: %s".formatted(th.getMessage()));
        errors.add(th);
    }

    /**
     * Creates a custom SPI classloader for the given element path if custom SPI dependencies are configured.
     * If no custom SPI is configured, returns the parent classloader.
     *
     * @param parent           the parent classloader
     * @param elementPath      the element path to create SPI classloader for
     * @return a classloader with the SPI dependencies, or the parent if no custom SPI
     */
    public ClassLoader createSpiClassLoaderFor(final ClassLoader parent, final Path elementPath) {

        final var spiClassPath = spiPaths.get(elementPath);

        if (spiClassPath == null) {
            log("%s uses default SPI. Not loading SPI".formatted(elementPath));
            return parent;
        }

        log("Resolving SPI for %s with %d coordinate(s): %s".formatted(elementPath, spiClassPath.size(), spiClassPath));

        return artifactLoader
                .findClassLoader(parent, repositories, spiClassPath)
                .orElseGet(() -> {
                    warn("No artifacts found for SPI coordinates: " + spiClassPath);
                    return parent;
                });

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

        log("Resolving artifact with dependencies: " + coordinates);

        final var artifacts = artifactLoader.findClasspathForArtifact(repositories, coordinates).toList();
        log("Found %d artifact(s) including dependencies %s".formatted(artifacts.size(), coordinates));

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
            log("Copied artifact: %s".formatted(fileName));
            deploymentFiles.add(destinationPath);

        }

    }

    public Attributes createAttributesForPath(final Attributes baseAttrs, final Path elementPath) {

        // Look up pre-computed attributes for this path
        final var resolved = attributePaths().getOrDefault(
                elementPath.toAbsolutePath(),
                Attributes.emptyAttributes()
        );

        // Create the base attributes found in the deployment
        final var builder = new SimpleAttributes.Builder()
                .from(baseAttrs)
                .from(resolved);

        // If the deployment is scoped to an Application, inject APPLICATION_ATTRIBUTE for all elements
        if (deployment().application() != null) {
            builder.setAttribute(Application.APPLICATION_ATTRIBUTE, deployment().application());
        }

        return builder.build();

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
     * Creates a temporary file with prefix and suffix. Logs the file to the {@link DeploymentContext#deploymentFiles()} such
     * that it can be reported and later cleaned up.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the {@link Path}
     */
    public Path createTempFile(final String prefix, final String suffix) {
        final var file = temporaryFiles.createTempFile(prefix, suffix);
        deploymentFiles.add(file);
        return file;
    }

    /**
     * Stores the supplied manifest for the given element path and applies any builtin SPIs it declares. If the
     * element path already has explicit SPI configuration in {@link #spiPaths()}, manifest-declared builtins are
     * skipped so that deployment-level overrides take precedence.
     *
     * @param manifest    the parsed manifest for the element
     * @param elementPath the root path of the individual element
     * @return {@code true} if manifest-declared builtin SPIs were injected into {@link #spiPaths()},
     *         {@code false} otherwise
     */
    public boolean applyManifest(final ElementManifestRecord manifest, final Path elementPath) {

        manifests.put(elementPath, manifest);

        if (!SystemVersion.UNKNOWN.equals(manifest.version())) {
            log("Manifest at %s: version=%s, revision=%s, buildTime=%s".formatted(
                    elementPath,
                    manifest.version().version(),
                    manifest.version().revision(),
                    manifest.version().timestamp()));
        }

        if (manifest.builtinSpis().isEmpty() || spiPaths.containsKey(elementPath)) {
            return false;
        }

        log("Applying %d manifest builtin SPI(s) for element at %s".formatted(
                manifest.builtinSpis().size(),
                elementPath)
        );

        for (final var name : manifest.builtinSpis()) {
            resolveBuiltinSpi(name).forEach(coord ->
                    spiPaths.computeIfAbsent(elementPath, k -> new HashSet<>()).add(coord)
            );
        }

        if (spiPaths.containsKey(elementPath)) {
            unconsumedSpiPaths.add(elementPath);
        }

        return true;

    }

    /**
     * Resolves a builtin SPI name to its Maven artifact coordinates. Logs a warning if the name is unknown.
     *
     * @param name the {@link BuiltinSpi} enum name
     * @return the list of Maven coordinate strings, or an empty list if unrecognised
     */
    public List<String> resolveBuiltinSpi(final String name) {
        try {
            return BuiltinSpi.valueOf(name).coordinates();
        } catch (IllegalArgumentException e) {
            warn("Unknown builtin SPI name: " + name);
            return List.of();
        }
    }

    /**
     * Records the association between a loaded {@link Element} and its on-disk {@link ElementPathRecord}.
     * Intended for use as an {@link ElementPathLoader.LoadConfiguration} {@code elementLoadedHandler}.
     *
     * @param path    the path from which the element was loaded
     * @param element the loaded element
     */
    public void recordElement(final Path path, final Element element) {
        elementPathsByElement.put(element, pathLoader.readElement(path));
    }

    /**
     * Reads and applies the manifest for the element at the given path, then creates the SPI classloader.
     * Intended for use as an {@link ElementPathLoader.SpiLoader} method reference.
     *
     * @param parent      the parent classloader
     * @param elementPath the path to the element
     * @return the SPI classloader, or the parent classloader if no custom SPI is configured
     */
    public ClassLoader loadSpiForPath(final ClassLoader parent, final Path elementPath) {

        applyManifest(pathLoader.readAndParseManifest(elementPath), elementPath);

        final var result = createSpiClassLoaderFor(parent, elementPath);

        if (unconsumedSpiPaths.remove(elementPath)) {
            log("Applied SPI to element at path: %s:%s".formatted(
                    elementPath.getFileSystem(),
                    elementPath
            ));
        } else if (parent == result) {
            log("Using default SPI for path: %s:%s".formatted(
                    elementPath.getFileSystem(),
                    elementPath
            ));
        } else {
            warn("Previously consumed SPI classpath to element at path %s:%s ".formatted(
                    elementPath.getFileSystem(),
                    elementPath
            ));
        }

        return result;

    }

    /**
     * Creates the final {@link Attributes} for the element at the given path and logs the result.
     * Intended for use as an {@link ElementPathLoader.AttributesLoader} method reference.
     *
     * @param baseAttrs   the base attributes passed by the loader
     * @param elementPath the path to the element
     * @return the resolved attributes
     */
    public Attributes loadAttributesForPath(final Attributes baseAttrs, final Path elementPath) {

        final var finalAttributes = createAttributesForPath(baseAttrs, elementPath);

        if (unconsumedAttributePaths.remove(elementPath)) {
            log("Applied attributes to element at path: %s:%s\n%s".formatted(
                    elementPath.getFileSystem(),
                    elementPath,
                    String.join(" -> \n", finalAttributes.getAttributeNames())
            ));
        }

        return finalAttributes;

    }

}

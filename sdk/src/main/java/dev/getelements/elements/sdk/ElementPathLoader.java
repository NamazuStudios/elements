package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ElementManifestRecord;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.lang.System.getenv;
import static java.lang.Thread.currentThread;
import static java.util.List.of;
import static java.util.Objects.requireNonNull;

/**
 * Used to load {@link Element} instances from local paths on disk. As a single Element may span multiple sources for
 * executable code, this provides loading semantics for code on disk in a structured manner. When loading
 * {@link Element}s, the loader will search a {@link Path} with the following rules.
 *
 * <ul>
 *     <li>If the {@link Path} can load using {@link java.nio.file.FileSystems#newFileSystem(Path)}, it will load</li>
 *     <li>If the {@link Path} is a directory, each subdirectory is scanned as a potential Element</li>
 *     <li>If the {@link Path} is not found or otherwise empty, it will return an empty {@link Stream}</li>
 *     <li>Each subdirectory containing Element configuration becomes an independent {@link Element}</li>
 *     <li>Element directory names are irrelevant. All {@link Element} metadata comes from the annotations.</li>
 * </ul>
 *
 * When searching each element directory, the ElementPathLoader will look for the following files/directories to determine the
 * makings of the {@link Element}.
 *
 * <ul>
 *    <li>dev.getelements.element.attributes.properties - custom attributes for the Element</li>
 *    <li>api - every file is assumed to be a jar file, shared among all Elements in the deployment</li>
 *    <li>spi - every file is assumed to be a jar file, Service Provider Interface specific to that Element</li>
 *    <li>lib - every file is assumed to be a jar file, specific to that Element only</li>
 *    <li>classpath - every file inside this directory is added to the classpath</li>
 * </ul>
 *
 * For example, let's say we have the following deployment structure with Elements defined:
 * <ul>
 *     <li>deployment/api/ - shared API jars</li>
 *     <li>deployment/foo/ - com.example.foo Element</li>
 *     <li>deployment/bar/ - com.example.bar Element</li>
 *     <li>deployment/baz/ - com.example.baz Element</li>
 * </ul>
 *
 * This will result in three independent {@link Element} instances, each with its own classloader hierarchy:
 * API → SPI → Implementation.
 *
 * Changes for 3.6 and up. The "api" directory is now supported. Any jars in this directory are shared among all
 * Elements in the deployment. This enables each Element to share common API jars without exposing its own
 * implementation jars to its peers. API jars should be as lean as absolutely possible, containing only
 * the interfaces and data types needed to interact with the Element's services.
 *
 * Changes for 3.7 and up. The "spi" directory is now supported, and the loading model has been simplified to a flat
 * structure. Each element directory is independent with no nesting. The classloader hierarchy for each Element is:
 * API (shared) → SPI (per-element) → Implementation (per-element). The SPI layer allows developers to attach Service
 * Provider Interface jars at deployment time, enabling Elements to communicate through a common API while maintaining
 * deployment-time flexibility for their service provider implementations.
 */
public interface ElementPathLoader {

    /**
     * The jar file extension.
     */
    String JAR_EXTENSION = "jar";

    /**
     * The Element container format (elm).
     */
    String ELM_EXTENSION = "elm";

    /**
     * The mime type for the ELM files.
     */
    String ELM_MIME_TYPE = "application/zip";

    /**
     * The environment variable for the element path.
     */
    String ELEMENT_PATH_ENV = "ELEMENTPATH";

    /**
     * The API directory.
     * @since 3.6
     */
    String API_DIR = "api";

    /**
     * The SPI directory.
     * @since 3.7
     */
    String SPI_DIR = "spi";

    /**
     * The library directory name.
     */
    String LIB_DIR = "lib";

    /**
     * The Classpath directory name.
     */
    String CLASSPATH_DIR = "classpath";

    /**
     * The name of the file which represents a particular {@link Element}'s {@link Attributes}. Uses the
     * {@link java.util.Properties} format when defining the {@link Attributes}. This allows for the separation of
     * code and configuration at deployment time.
     */
    String ATTRIBUTES_PROPERTIES_FILE = "dev.getelements.element.attributes.properties";

    /**
     * The name of the manifest file for a particular {@link Element}. Uses the {@link java.util.Properties} format.
     * The manifest may declare version metadata and a list of builtin SPI names the element requires. If absent, the
     * element is treated as having no manifest.
     *
     * @since 3.7
     */
    String MANIFEST_PROPERTIES_FILE = "dev.getelements.element.manifest.properties";

    /**
     * Configuration for loading Elements from paths. Provides sensible defaults for all optional parameters.
     * Use {@link #builder()} to construct instances with a fluent API.
     *
     * @param registry the registry to load Elements into (required)
     * @param paths the paths to scan for Elements (required)
     * @param parent the parent classloader for delegation (nullable, defaults to bootstrap)
     * @param baseClassLoader the base classloader for selective type borrowing (nullable, defaults to this class's classloader)
     * @param spiLoader the SPI loader
     * @param attributesProvider function to provide Attributes for each Element path (nullable, defaults to properties file reader)
     * @since 3.7
     */
    record LoadConfiguration(
            MutableElementRegistry registry,
            Collection<Path> paths,
            ClassLoader parent,
            ClassLoader baseClassLoader,
            SpiLoader spiLoader,
            AttributesLoader attributesProvider) {

        /**
         * Creates a new builder for LoadConfiguration.
         *
         * @return a new builder instance
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Builder for LoadConfiguration with fluent API and automatic defaults.
         */
        public static final class Builder {

            private MutableElementRegistry registry;

            private Collection<Path> paths;

            private ClassLoader parent;

            private ClassLoader baseClassLoader;

            private SpiLoader spiLoader = (parent, path) -> parent;

            private AttributesLoader attributesLoader = (attributes, path) -> attributes;

            private Builder() {}

            /**
             * Sets the registry to load Elements into.
             *
             * @param registry the registry (required)
             * @return this builder
             */
            public Builder registry(final MutableElementRegistry registry) {
                this.registry = requireNonNull(registry, "registry");
                return this;
            }

            /**
             * Sets the paths to scan for Elements.
             *
             * @param paths the collection of paths (required)
             * @return this builder
             */
            public Builder paths(final Collection<Path> paths) {
                this.paths = requireNonNull(paths, "paths");
                return this;
            }

            /**
             * Sets a single path to scan for Elements.
             *
             * @param path the path (required)
             * @return this builder
             */
            public Builder path(final Path path) {
                this.paths = Set.of(requireNonNull(path, "path"));
                return this;
            }

            /**
             * Sets the parent classloader for delegation.
             *
             * @param parent the parent classloader (null for bootstrap)
             * @return this builder
             */
            public Builder parent(final ClassLoader parent) {
                this.parent = parent;
                return this;
            }

            /**
             * Sets the base classloader for selective type borrowing.
             *
             * @param baseClassLoader the base classloader
             * @return this builder
             */
            public Builder baseClassLoader(final ClassLoader baseClassLoader) {
                this.baseClassLoader = baseClassLoader;
                return this;
            }

            /**
             * Provides the SPI loader.
             *
             * @param spiLoader the spi loader
             * @return this instance
             */
            public Builder spiProvider(final SpiLoader spiLoader) {

                this.spiLoader = spiLoader == null
                        ? (parent, path) -> parent
                        : spiLoader;

                return this;

            }

            /**
             * Sets the function to provide Attributes for each Element path.
             *
             * @param attributesLoader the attributes provider function
             * @return this builder
             */
            public Builder attributesLoader(final AttributesLoader attributesLoader) {

                this.attributesLoader = attributesLoader == null
                    ? (attributes, path) -> attributes
                    : attributesLoader;

                return this;

            }

            /**
             * Builds the LoadConfiguration with defaults applied for unspecified optional parameters.
             *
             * @return the configured LoadConfiguration
             * @throws NullPointerException if required parameters (registry, paths) are not set
             */
            public LoadConfiguration build() {

                requireNonNull(registry, "registry must be set");
                requireNonNull(paths, "paths must be set");

                final var finalBaseClassLoader = baseClassLoader != null
                        ? baseClassLoader
                        : currentThread().getContextClassLoader();

                return new LoadConfiguration(
                        registry,
                        paths,
                        parent,
                        finalBaseClassLoader,
                        spiLoader,
                        attributesLoader
                );

            }
        }

    }

    /**
     * Loads Elements using the specified configuration. This is the canonical load method;
     * all other load methods delegate to this one. Implementations should override this method
     * to provide their Element loading logic.
     *
     * @param config the load configuration
     * @return a stream of loaded Elements
     * @since 3.7
     */
    Stream<Element> load(LoadConfiguration config);

    /**
     * Loads all {@link Element} into the supplied registry, using the globally configured ELEMENTPATH as the
     * source location.
     *
     * @param registry the registry to receive the loaded {@link Element}s
     */
    default Stream<Element> load(final MutableElementRegistry registry) {
        return load(LoadConfiguration.builder()
                .registry(registry)
                .path(Path.of(getenv(ELEMENT_PATH_ENV)))
                .build());
    }

    /**
     * Loads all {@link Element}s from the supplied {@link Path} into the supplied {@link ElementRegistry}.
     * Creates {@link ElementLoader} instances for each Element discovered in the path. Builds an API classloader
     * from the path and manages its lifecycle with reference counting. The default implementation takes a copy
     * of the returned {@link Element}s and returns that stream.
     *
     * <p>
     * The classloader configuration uses the bootstrap classloader as the Elements' parent classloader
     * (via the API classloader built from the path), and this class's classloader as the base classloader
     * for selective type borrowing.
     * </p>
     *
     * @param registry the registry to use for loading the {@link Element} instances
     * @param path the {@link Path}
     * @return a {@link Stream} of {@link Element} instances
     * @see ElementLoader
     */
    default Stream<Element> load(
            final MutableElementRegistry registry,
            final Path path) {
        return load(LoadConfiguration.builder()
                .registry(registry)
                .path(path)
                .build());
    }

    /**
     * Loads all {@link Element}s from the supplied collection of {@link Path}s into the supplied {@link ElementRegistry}.
     * Creates {@link ElementLoader} instances for each Element discovered across all paths. Builds a single shared
     * API classloader from all paths and manages its lifecycle with reference counting. Elements from all paths
     * share the same API classloader, enabling them to communicate through common APIs. The implementation
     * iterates through each path, loading elements and collecting them into a single stream.
     *
     * <p>
     * The classloader configuration uses the bootstrap classloader as the Elements' parent classloader
     * (via the shared API classloader built from all paths), and this class's classloader as the base classloader
     * for selective type borrowing.
     * </p>
     *
     * @param registry the registry to use for loading the {@link Element} instances
     * @param paths the collection of {@link Path}s to scan for Elements
     * @return a {@link Stream} of {@link Element} instances from all paths
     * @see ElementLoader
     */
    default Stream<Element> load(
            final MutableElementRegistry registry,
            final Collection<Path> paths) {
        return load(LoadConfiguration.builder()
                .registry(registry)
                .paths(paths)
                .build());
    }

    /**
     * Loads all {@link Element}s from the supplied {@link Path} into the supplied {@link ElementRegistry}.
     * Creates {@link ElementLoader} instances for each Element discovered in the path.
     *
     * <p>
     * The classloader configuration uses the explicitly specified parent classloader as the Elements'
     * parent classloader, and this class's classloader as the base classloader for selective type borrowing.
     * </p>
     *
     * @param registry the registry to use for loading the {@link Element} instances
     * @param path the {@link Path}
     * @param parent the parent {@link ClassLoader} that forms the delegation parent of the implementation classloader
     * @return a {@link Stream} of {@link Element} instances
     * @see ElementLoader
     */
    default Stream<Element> load(
            final MutableElementRegistry registry,
            final Path path,
            final ClassLoader parent) {
        return load(LoadConfiguration.builder()
                .registry(registry)
                .path(path)
                .parent(parent)
                .build()
        );
    }

    /**
     * Loads all {@link Element}s from the supplied {@link Path} into the supplied {@link ElementRegistry}.
     * Creates {@link ElementLoader} instances for each Element discovered in the path.
     *
     * <p>
     * The classloader configuration uses the explicitly specified parent classloader as the Elements'
     * parent classloader (forming the delegation parent of the implementation classloader), and the
     * explicitly specified base classloader for selective type borrowing.
     * </p>
     *
     * @param registry the registry to use for loading the {@link Element} instances
     * @param path the {@link Path}
     * @param parent the parent {@link ClassLoader} that forms the delegation parent of the implementation classloader
     * @param baseClassLoader the base {@link ClassLoader} used for selective type borrowing
     * @return a {@link Stream} of {@link Element} instances
     * @since 3.7
     * @see ElementLoader
     */
    default Stream<Element> load(
            final MutableElementRegistry registry,
            final Path path,
            final ClassLoader parent,
            final ClassLoader baseClassLoader) {
        return load(LoadConfiguration.builder()
                .registry(registry)
                .path(path)
                .parent(parent)
                .baseClassLoader(baseClassLoader)
                .build());
    }

    /**
     * Builds a classloader containing API jars from a collection of paths. Each path may be:
     * <ul>
     *     <li>An ELM file (zip) - scans for {@code api/} directory inside</li>
     *     <li>A directory - scans for {@code api/} subdirectory</li>
     * </ul>
     * All API jars found are combined into a single URLClassLoader with the specified parent.
     * If no API jars are found, returns a URLClassLoader with an empty URL array.
     * <p>
     * The returned URLClassLoader must be closed when no longer needed to release any resources
     * (such as open zip file systems for ELM files).
     *
     * @param parent the parent ClassLoader, or null to use the bootstrap classloader
     * @param paths collection of paths to scan for API jars
     * @return a URLClassLoader with all API jars (empty URL array if no APIs found)
     * @since 3.7
     */
    URLClassLoader buildApiClassLoader(ClassLoader parent, Collection<Path> paths);

    /**
     * Builds a classloader containing API jars from one or more paths.
     * Convenience method that delegates to {@link #buildApiClassLoader(ClassLoader, Collection)}.
     * <p>
     * The returned URLClassLoader must be closed when no longer needed.
     *
     * @param parent the parent ClassLoader, or null to use the bootstrap classloader
     * @param paths one or more paths to scan for API jars
     * @return a URLClassLoader with all API jars (empty URL array if no APIs found)
     * @since 3.7
     */
    default URLClassLoader buildApiClassLoader(final ClassLoader parent, final Path... paths) {
        return buildApiClassLoader(parent, of(paths));
    }

    /**
     * Finds the SPI (service provider implementation) for a path to a single Element. This is intended to side-load
     * the SPIs separate from the Element itself just in time for loading.
     *
     * <p>
     * The {@link Path} specified must be one of the following:
     * <ul>
     *     <li>The root directory of the Element itself. In this case, it scans for all jars in path/spi.</li>
     *     <li>The root directory of the SPI, In this case, it scans for all jars in the path itself.</li>
     * </ul>
     *
     * <p>
     * All SPI jars found are combined into a single URLClassLoader with the specified parent.
     *
     * <p>
     *
     * @param parent the parent ClassLoader, or null to use the bootstrap classloader
     * @param path collection of paths to scan for SPI jars
     * @return a URLClassLoader with all SPI jars, or an empty {@link Optional} if no SPI can be found.
     * @since 3.7
     */
    Optional<ClassLoader> findSpiClassLoader(ClassLoader parent, Path path);

    /**
     * Reads the manifest for a single {@link Element} at the given path. The path must point to the root directory of
     * one individual element (i.e. a subdirectory within a deployment, not the deployment root itself). The
     * implementation should look for {@link #MANIFEST_PROPERTIES_FILE} under that path and return its contents as
     * {@link Attributes}. If the file is absent or unreadable, an empty {@link Attributes} must be returned.
     *
     * <p>
     * This method must work for paths in any {@link java.nio.file.FileSystem}, including zip/ELM-backed file systems.
     * </p>
     *
     * @param path the root directory of a single Element
     * @return the manifest {@link Attributes}, or empty {@link Attributes} if no manifest is present
     * @since 3.7
     */
    Attributes readManifest(Path path);

    /**
     * Reads the {@link ElementManifestRecord} from the {@link Path}.
     *
     * @param path the {@link Path}
     */
    default ElementManifestRecord readAndParseManifest(final Path path) {
        final var attributes = readManifest(path);
        return ElementManifestRecord.from(attributes);
    }

    /**
     * Loads a ClassLoader given the parent ClassLoader and the path of the Element itself. This is useful for when you
     * need to externalize an SPI which is not bundled in an ELM file nor placed on disk in the standard location. THis
     * makes it possible to separate the SPI from an ELM distribution on a per-Element basis. If no modifications to the
     * Classpath are necessary, then it is safe to simply return the ClassLoader provided.
     */
    @FunctionalInterface
    interface SpiLoader extends BiFunction<ClassLoader, Path, ClassLoader> {}

    /**
     * Loads a properties file given the base attributes and the {@link Path} to the Element's root. The loader will
     * try to load the {@link Attributes} from the file at "dev.getelements.element.attributes.properties" under the
     * path. If the file does not exist, then the attributes will be empty. The PropertiesFileLoader implementation will
     * have the opportunity to supply its own modifications to the supplied Attributes.
     */
    @FunctionalInterface
    interface AttributesLoader extends BiFunction<Attributes, Path, Attributes> {}

    /**
     * Creates a new instance of the {@link ElementPathLoader} using the system default SPI.
     *
     * @return new {@link ElementPathLoader}
     */
    static ElementPathLoader newDefaultInstance() {
        return ServiceLoader
                .load(ElementPathLoader.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new SdkException("No ElementPathLoader SPI Available."))
                .get();
    }

}

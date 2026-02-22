package dev.getelements.elements.sdk.deployment;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.system.ElementArtifactRepository;
import dev.getelements.elements.sdk.model.system.ElementPackageDefinition;
import dev.getelements.elements.sdk.model.system.ElementPathDefinition;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Configuration for loading a transient deployment from Maven artifact coordinates.
 * The service will assign an ID and manage the deployment lifecycle.
 *
 * @param application            the application context (null for system-wide deployment)
 * @param elmLargeObjectId          the id of a {@link LargeObject} to hold the ELM (may be null)
 * @param pathAttributes         custom attributes per element path (may be null)
 * @param elements               list of path-based element definitions (may be null)
 * @param packages               list of package-based element definitions (may be null)
 * @param useDefaultRepositories whether to include default Maven repositories
 * @param repositories           additional artifact repositories for resolution (may be empty)
 */
public record TransientDeploymentRequest(

        Application application,

        String elmLargeObjectId,

        Map<String, List<String>> pathSpiBuiltins,

        Map<String, List<String>> pathSpiClasspath,

        Map<String, Map<String, Object>> pathAttributes,

        @Valid
        List<ElementPathDefinition> elements,

        @Valid
        List<ElementPackageDefinition> packages,

        boolean useDefaultRepositories,

        @Valid
        List<ElementArtifactRepository> repositories

) {

    /**
     * Canonical constructor that ensures all collections are immutable copies.
     */
    public TransientDeploymentRequest {
        // Create immutable copies of path attributes with nested maps

        if (pathSpiBuiltins != null) {
            pathSpiBuiltins = pathSpiBuiltins.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue() == null
                                    ? List.of()
                                    : List.copyOf(entry.getValue())
                    ));
        }

        if (pathSpiClasspath != null) {
            pathSpiClasspath = pathSpiClasspath.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue() == null
                                    ? List.of()
                                    : List.copyOf(entry.getValue())
                    ));
        }

        if (pathAttributes != null) {
            pathAttributes = pathAttributes.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue() == null
                                    ? Map.of()
                                    : Map.copyOf(entry.getValue())
                    ));
        }

        // Create immutable copies of lists
        elements = elements == null ? null : List.copyOf(elements);
        packages = packages == null ? null : List.copyOf(packages);
        repositories = repositories == null ? null : List.copyOf(repositories);

    }

    /**
     * Creates a new builder for TransientDeploymentRequest.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for TransientDeploymentRequest.
     */
    public static final class Builder {
        private Application application;
        private String elmLargeObjectId;
        private Map<String, List<String>> pathSpiBuiltins;
        private Map<String, List<String>> pathSpiClasspath;
        private Map<String, Map<String, Object>> pathAttributes;
        private List<ElementPathDefinition> elements;
        private List<ElementPackageDefinition> packages;
        private boolean useDefaultRepositories = true;
        private List<ElementArtifactRepository> repositories;

        private Builder() {
        }

        /**
         * Sets the application context.
         *
         * @param application the application context (null for system-wide deployment)
         * @return this builder
         */
        public Builder application(final Application application) {
            this.application = application;
            return this;
        }

        /**
         * Sets the large object ID for the ELM.
         *
         * @param elmLargeObjectId the id of a {@link dev.getelements.elements.sdk.model.largeobject.LargeObject} to hold the ELM
         * @return this builder
         */
        public Builder elmLargeObjectId(final String elmLargeObjectId) {
            this.elmLargeObjectId = elmLargeObjectId;
            return this;
        }

        /**
         * Adds SPI builtins for a specific element path.
         *
         * @param path     the element path
         * @param builtins the SPI builtin names
         * @return this builder
         */
        public Builder addPathSpiBuiltins(final String path, final List<String> builtins) {
            if (this.pathSpiBuiltins == null) {
                this.pathSpiBuiltins = new java.util.HashMap<>();
            }
            this.pathSpiBuiltins.put(path, builtins);
            return this;
        }

        /**
         * Adds a single SPI builtin for a specific element path.
         *
         * @param path    the element path
         * @param builtin the SPI builtin name
         * @return this builder
         */
        public Builder addPathSpiBuiltin(final String path, final String builtin) {
            if (this.pathSpiBuiltins == null) {
                this.pathSpiBuiltins = new java.util.HashMap<>();
            }
            this.pathSpiBuiltins.computeIfAbsent(path, k -> new java.util.ArrayList<>()).add(builtin);
            return this;
        }

        /**
         * Adds a single SPI class path entry for a specific element path.
         *
         * @param path      the element path
         * @param classPath the SPI class path entry
         * @return this builder
         */
        public Builder addPathSpiClassPath(final String path, final String classPath) {
            if (this.pathSpiClasspath == null) {
                this.pathSpiClasspath = new java.util.HashMap<>();
            }
            this.pathSpiClasspath.computeIfAbsent(path, k -> new java.util.ArrayList<>()).add(classPath);
            return this;
        }

        /**
         * Sets the path attributes map.
         *
         * @param pathAttributes custom attributes per element path
         * @return this builder
         */
        public Builder pathAttributes(final Map<String, Map<String, Object>> pathAttributes) {
            this.pathAttributes = pathAttributes;
            return this;
        }

        /**
         * Adds attributes for a specific element path.
         *
         * @param path       the element path
         * @param attributes the attributes to add for this path
         * @return this builder
         */
        public Builder addPathAttributes(final String path, final Map<String, Object> attributes) {
            if (this.pathAttributes == null) {
                this.pathAttributes = new java.util.HashMap<>();
            }
            this.pathAttributes.put(path, attributes);
            return this;
        }

        /**
         * Sets the SPI Classpath for a specific path within the deployment.
         *
         * @param path      the path
         * @param classpath the classpath
         * @return this instance
         */
        public Builder addPathSpiClasspath(final String path, final List<String> classpath) {

            if (this.pathSpiClasspath == null) {
                this.pathSpiClasspath = new java.util.HashMap<>();
            }

            this.pathSpiClasspath.put(path, classpath);
            return this;
        }

        /**
         * Adds a single attribute for a specific element path.
         *
         * @param path  the element path
         * @param key   the attribute key
         * @param value the attribute value
         * @return this builder
         */
        public Builder addPathAttribute(final String path, final String key, final Object value) {
            if (this.pathAttributes == null) {
                this.pathAttributes = new java.util.HashMap<>();
            }
            this.pathAttributes
                    .computeIfAbsent(path, k -> new java.util.HashMap<>())
                    .put(key, value);
            return this;
        }

        /**
         * Sets the list of path-based element definitions.
         *
         * @param elements list of path-based element definitions
         * @return this builder
         */
        public Builder elements(final List<ElementPathDefinition> elements) {
            this.elements = elements;
            return this;
        }

        /**
         * Adds a single path-based element definition.
         *
         * @param element the element definition to add
         * @return this builder
         */
        public Builder addElement(final ElementPathDefinition element) {
            if (this.elements == null) {
                this.elements = new java.util.ArrayList<>();
            }
            this.elements.add(element);
            return this;
        }

        /**
         * Starts building an element path definition using a sub-builder.
         *
         * @return a new ElementPathDefinitionBuilder
         */
        public ElementPathDefinitionBuilder<Builder> elementPath() {
            return new ElementPathDefinitionBuilder<>(this, this::addElement);
        }

        /**
         * Sets the list of package-based element definitions.
         *
         * @param packages list of package-based element definitions
         * @return this builder
         */
        public Builder packages(final List<ElementPackageDefinition> packages) {
            this.packages = packages;
            return this;
        }

        /**
         * Adds a single package-based element definition.
         *
         * @param packageDef the package definition to add
         * @return this builder
         */
        public Builder addPackage(final ElementPackageDefinition packageDef) {
            if (this.packages == null) {
                this.packages = new java.util.ArrayList<>();
            }
            this.packages.add(packageDef);
            return this;
        }

        /**
         * Starts building an element package definition using a sub-builder.
         *
         * @return a new ElementPackageDefinitionBuilder
         */
        public ElementPackageDefinitionBuilder<Builder> elementPackage() {
            return new ElementPackageDefinitionBuilder<>(this, this::addPackage);
        }

        /**
         * Sets whether to use default Maven repositories.
         *
         * @param useDefaultRepositories true to include default repositories
         * @return this builder
         */
        public Builder useDefaultRepositories(final boolean useDefaultRepositories) {
            this.useDefaultRepositories = useDefaultRepositories;
            return this;
        }

        /**
         * Sets the list of artifact repositories.
         *
         * @param repositories list of artifact repositories
         * @return this builder
         */
        public Builder repositories(final List<ElementArtifactRepository> repositories) {
            this.repositories = repositories;
            return this;
        }

        /**
         * Adds a single artifact repository.
         *
         * @param repository the artifact repository to add
         * @return this builder
         */
        public Builder addRepository(final ElementArtifactRepository repository) {
            if (this.repositories == null) {
                this.repositories = new java.util.ArrayList<>();
            }
            this.repositories.add(repository);
            return this;
        }

        /**
         * Builds the TransientDeploymentRequest.
         *
         * @return a new TransientDeploymentRequest instance
         */
        public TransientDeploymentRequest build() {
            return new TransientDeploymentRequest(
                    application,
                    elmLargeObjectId,
                    pathSpiBuiltins,
                    pathSpiClasspath,
                    pathAttributes,
                    elements,
                    packages,
                    useDefaultRepositories,
                    repositories
            );
        }

        /**
         * Builder for constructing {@link ElementPathDefinition} instances with a fluent API.
         *
         * @param <ParentT> the parent builder type
         */
        public static final class ElementPathDefinitionBuilder<ParentT> {

            private final ParentT parent;
            private final Consumer<ElementPathDefinition> consumer;

            private String path;
            private List<String> apiArtifacts = new ArrayList<>();
            private List<String> spiBuiltins = new ArrayList<>();
            private List<String> spiArtifacts = new ArrayList<>();
            private List<String> elementArtifacts = new ArrayList<>();
            private Map<String, Object> attributes = new HashMap<>();

            private ElementPathDefinitionBuilder(
                    final ParentT parent,
                    final Consumer<ElementPathDefinition> consumer) {
                this.parent = parent;
                this.consumer = consumer;
            }

            /**
             * Sets the element path.
             *
             * @param path the element path
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> path(final String path) {
                this.path = path;
                return this;
            }

            /**
             * Sets the API artifacts list.
             *
             * @param apiArtifacts the API artifacts list
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> apiArtifacts(final List<String> apiArtifacts) {
                this.apiArtifacts = apiArtifacts != null ? new ArrayList<>(apiArtifacts) : new ArrayList<>();
                return this;
            }

            /**
             * Adds an API artifact.
             *
             * @param artifact the API artifact
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> addApiArtifact(final String artifact) {
                this.apiArtifacts.add(artifact);
                return this;
            }

            /**
             * Sets the SPI builtins list.
             *
             * @param spiBuiltins the SPI builtins list
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> spiBuiltins(final List<String> spiBuiltins) {
                this.spiBuiltins = spiBuiltins != null ? new ArrayList<>(spiBuiltins) : new ArrayList<>();
                return this;
            }

            /**
             * Adds a SPI builtin name.
             *
             * @param builtin the SPI builtin name
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> addSpiBuiltin(final String builtin) {
                this.spiBuiltins.add(builtin);
                return this;
            }

            /**
             * Sets the SPI artifacts list.
             *
             * @param spiArtifacts the SPI artifacts list
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> spiArtifacts(final List<String> spiArtifacts) {
                this.spiArtifacts = spiArtifacts != null ? new ArrayList<>(spiArtifacts) : new ArrayList<>();
                return this;
            }

            /**
             * Adds an SPI artifact.
             *
             * @param artifact the SPI artifact
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> addSpiArtifact(final String artifact) {
                this.spiArtifacts.add(artifact);
                return this;
            }

            /**
             * Adds multiple SPI artifacts.
             *
             * @param artifacts the SPI artifacts
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> addSpiArtifacts(final Iterable<String> artifacts) {
                artifacts.forEach(this.spiArtifacts::add);
                return this;
            }

            /**
             * Sets the element artifacts list.
             *
             * @param elementArtifacts the element artifacts list
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> elementArtifacts(final List<String> elementArtifacts) {
                this.elementArtifacts = elementArtifacts != null ? new ArrayList<>(elementArtifacts) : new ArrayList<>();
                return this;
            }

            /**
             * Adds an element artifact.
             *
             * @param artifact the element artifact
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> addElementArtifact(final String artifact) {
                this.elementArtifacts.add(artifact);
                return this;
            }

            /**
             * Adds multiple element artifacts.
             *
             * @param artifacts the element artifacts
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> addElementArtifacts(final Iterable<String> artifacts) {
                artifacts.forEach(this.elementArtifacts::add);
                return this;
            }

            /**
             * Sets the attributes map.
             *
             * @param attributes the attributes map
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> attributes(final Map<String, Object> attributes) {
                this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
                return this;
            }

            /**
             * Adds an attribute.
             *
             * @param key the attribute key
             * @param value the attribute value
             * @return this builder
             */
            public ElementPathDefinitionBuilder<ParentT> attribute(final String key, final Object value) {
                this.attributes.put(key, value);
                return this;
            }

            /**
             * Builds the ElementPathDefinition instance.
             *
             * @return a new ElementPathDefinition
             */
            public ElementPathDefinition build() {
                return new ElementPathDefinition(
                        path,
                        apiArtifacts.isEmpty() ? null : apiArtifacts,
                        spiBuiltins.isEmpty() ? null : spiBuiltins,
                        spiArtifacts.isEmpty() ? null : spiArtifacts,
                        elementArtifacts.isEmpty() ? null : elementArtifacts,
                        attributes.isEmpty() ? null : attributes
                );
            }

            /**
             * Builds the ElementPathDefinition and returns to the parent builder.
             *
             * @return the parent builder
             */
            public ParentT endElementPath() {
                consumer.accept(build());
                return parent;
            }
        }

        /**
         * Builder for constructing {@link ElementPackageDefinition} instances with a fluent API.
         *
         * @param <ParentT> the parent builder type
         */
        public static final class ElementPackageDefinitionBuilder<ParentT> {

            private final ParentT parent;
            private final Consumer<ElementPackageDefinition> consumer;

            private String elmArtifact;
            private Map<String, List<String>> pathSpiBuiltins = new HashMap<>();
            private Map<String, List<String>> pathSpiClassPaths = new HashMap<>();
            private Map<String, Map<String, Object>> pathAttributes = new HashMap<>();

            private ElementPackageDefinitionBuilder(
                    final ParentT parent,
                    final Consumer<ElementPackageDefinition> consumer) {
                this.parent = parent;
                this.consumer = consumer;
            }

            /**
             * Sets the ELM artifact.
             *
             * @param elmArtifact the ELM artifact
             * @return this builder
             */
            public ElementPackageDefinitionBuilder<ParentT> elmArtifact(final String elmArtifact) {
                this.elmArtifact = elmArtifact;
                return this;
            }

            /**
             * Sets the path SPI builtins map.
             *
             * @param pathSpiBuiltins the path SPI builtins map
             * @return this builder
             */
            public ElementPackageDefinitionBuilder<ParentT> pathSpiBuiltins(
                    final Map<String, List<String>> pathSpiBuiltins) {
                this.pathSpiBuiltins = pathSpiBuiltins != null ?
                        new HashMap<>(pathSpiBuiltins) : new HashMap<>();
                return this;
            }

            /**
             * Adds a path SPI builtin entry.
             *
             * @param path the element path
             * @param builtins the SPI builtin names
             * @return this builder
             */
            public ElementPackageDefinitionBuilder<ParentT> pathSpiBuiltin(
                    final String path,
                    final List<String> builtins) {
                this.pathSpiBuiltins.put(path, builtins);
                return this;
            }

            /**
             * Adds a single builtin name to a path's SPI builtins.
             *
             * @param path the element path
             * @param builtin the SPI builtin name to add
             * @return this builder
             */
            public ElementPackageDefinitionBuilder<ParentT> addPathSpiBuiltin(
                    final String path,
                    final String builtin) {
                this.pathSpiBuiltins
                        .computeIfAbsent(path, k -> new ArrayList<>())
                        .add(builtin);
                return this;
            }

            /**
             * Sets the path SPI class paths map.
             *
             * @param pathSpiClassPaths the path SPI class paths map
             * @return this builder
             */
            public ElementPackageDefinitionBuilder<ParentT> pathSpiClassPaths(
                    final Map<String, List<String>> pathSpiClassPaths) {
                this.pathSpiClassPaths = pathSpiClassPaths != null ?
                        new HashMap<>(pathSpiClassPaths) : new HashMap<>();
                return this;
            }

            /**
             * Adds a path SPI class path entry.
             *
             * @param path the element path
             * @param classPaths the SPI class paths
             * @return this builder
             */
            public ElementPackageDefinitionBuilder<ParentT> pathSpiClassPath(
                    final String path,
                    final List<String> classPaths) {
                this.pathSpiClassPaths.put(path, classPaths);
                return this;
            }

            /**
             * Adds a single class path to a path's SPI class paths.
             *
             * @param path the element path
             * @param classPath the SPI class path to add
             * @return this builder
             */
            public ElementPackageDefinitionBuilder<ParentT> addPathSpiClassPath(
                    final String path,
                    final String classPath) {
                this.pathSpiClassPaths
                        .computeIfAbsent(path, k -> new ArrayList<>())
                        .add(classPath);
                return this;
            }

            /**
             * Sets the path attributes map.
             *
             * @param pathAttributes the path attributes map
             * @return this builder
             */
            public ElementPackageDefinitionBuilder<ParentT> pathAttributes(
                    final Map<String, Map<String, Object>> pathAttributes) {
                this.pathAttributes = pathAttributes != null ? new HashMap<>(pathAttributes) : new HashMap<>();
                return this;
            }

            /**
             * Adds a path attribute entry.
             *
             * @param path the element path
             * @param key the attribute key
             * @param value the attribute value
             * @return this builder
             */
            public ElementPackageDefinitionBuilder<ParentT> pathAttribute(
                    final String path,
                    final String key,
                    final Object value) {
                this.pathAttributes
                        .computeIfAbsent(path, k -> new HashMap<>())
                        .put(key, value);
                return this;
            }

            /**
             * Builds the ElementPackageDefinition instance.
             *
             * @return a new ElementPackageDefinition
             */
            public ElementPackageDefinition build() {
                return new ElementPackageDefinition(
                        elmArtifact,
                        pathSpiBuiltins.isEmpty() ? null : pathSpiBuiltins,
                        pathSpiClassPaths.isEmpty() ? null : pathSpiClassPaths,
                        pathAttributes.isEmpty() ? null : pathAttributes
                );
            }

            /**
             * Builds the ElementPackageDefinition and returns to the parent builder.
             *
             * @return the parent builder
             */
            public ParentT endElementPackage() {
                consumer.accept(build());
                return parent;
            }
        }
    }
}

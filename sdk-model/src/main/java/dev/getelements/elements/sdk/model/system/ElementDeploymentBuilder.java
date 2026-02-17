package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for constructing {@link ElementDeployment} instances with a fluent API.
 * Provides sub-builders for complex nested structures.
 */
public final class ElementDeploymentBuilder {

    private String id;
    private Application application;
    private LargeObjectReference elm;
    private Map<String, List<String>> pathSpiClassPaths = new HashMap<>();
    private Map<String, Map<String, Object>> pathAttributes = new HashMap<>();
    private List<ElementPathDefinition> elements = new ArrayList<>();
    private List<ElementPackageDefinition> packages = new ArrayList<>();
    private boolean useDefaultRepositories;
    private List<ElementArtifactRepository> repositories = new ArrayList<>();
    private ElementDeploymentState state;
    private long version;

    private ElementDeploymentBuilder() {}

    /**
     * Creates a new builder instance.
     *
     * @return a new ElementDeploymentBuilder
     */
    public static ElementDeploymentBuilder builder() {
        return new ElementDeploymentBuilder();
    }

    /**
     * Sets the deployment ID.
     *
     * @param id the deployment ID
     * @return this builder
     */
    public ElementDeploymentBuilder id(final String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the application context.
     *
     * @param application the application
     * @return this builder
     */
    public ElementDeploymentBuilder application(final Application application) {
        this.application = application;
        return this;
    }

    /**
     * Sets the ELM large object reference.
     *
     * @param elm the ELM large object reference
     * @return this builder
     */
    public ElementDeploymentBuilder elm(final LargeObjectReference elm) {
        this.elm = elm;
        return this;
    }

    /**
     * Sets the path SPI class paths map.
     *
     * @param pathSpiClassPaths the path SPI class paths map
     * @return this builder
     */
    public ElementDeploymentBuilder pathSpiClassPaths(final Map<String, List<String>> pathSpiClassPaths) {
        this.pathSpiClassPaths = pathSpiClassPaths != null ? new HashMap<>(pathSpiClassPaths) : new HashMap<>();
        return this;
    }

    /**
     * Adds a path SPI class path entry.
     *
     * @param path the element path
     * @param classPaths the SPI class paths
     * @return this builder
     */
    public ElementDeploymentBuilder addPathSpiClassPath(final String path, final List<String> classPaths) {
        this.pathSpiClassPaths.put(path, classPaths);
        return this;
    }

    /**
     * Sets the path attributes map.
     *
     * @param pathAttributes the path attributes map
     * @return this builder
     */
    public ElementDeploymentBuilder pathAttributes(final Map<String, Map<String, Object>> pathAttributes) {
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
    public ElementDeploymentBuilder addPathAttribute(final String path, final String key, final Object value) {
        this.pathAttributes
                .computeIfAbsent(path, k -> new HashMap<>())
                .put(key, value);
        return this;
    }

    /**
     * Sets the elements list.
     *
     * @param elements the elements list
     * @return this builder
     */
    public ElementDeploymentBuilder elements(final List<ElementPathDefinition> elements) {
        this.elements = elements != null ? new ArrayList<>(elements) : new ArrayList<>();
        return this;
    }

    /**
     * Adds an element path definition.
     *
     * @param element the element path definition
     * @return this builder
     */
    public ElementDeploymentBuilder addElement(final ElementPathDefinition element) {
        this.elements.add(element);
        return this;
    }

    /**
     * Starts building an element path definition using a sub-builder.
     *
     * @return a new ElementPathDefinitionBuilder
     */
    public ElementPathDefinitionBuilder<ElementDeploymentBuilder> elementPath() {
        return new ElementPathDefinitionBuilder<>(this, this::addElement);
    }

    /**
     * Sets the packages list.
     *
     * @param packages the packages list
     * @return this builder
     */
    public ElementDeploymentBuilder packages(final List<ElementPackageDefinition> packages) {
        this.packages = packages != null ? new ArrayList<>(packages) : new ArrayList<>();
        return this;
    }

    /**
     * Adds an element package definition.
     *
     * @param pkg the element package definition
     * @return this builder
     */
    public ElementDeploymentBuilder addPackage(final ElementPackageDefinition pkg) {
        this.packages.add(pkg);
        return this;
    }

    /**
     * Starts building an element package definition using a sub-builder.
     *
     * @return a new ElementPackageDefinitionBuilder
     */
    public ElementPackageDefinitionBuilder<ElementDeploymentBuilder> elementPackage() {
        return new ElementPackageDefinitionBuilder<>(this, this::addPackage);
    }

    /**
     * Sets whether to use default repositories.
     *
     * @param useDefaultRepositories whether to use default repositories
     * @return this builder
     */
    public ElementDeploymentBuilder useDefaultRepositories(final boolean useDefaultRepositories) {
        this.useDefaultRepositories = useDefaultRepositories;
        return this;
    }

    /**
     * Sets the repositories list.
     *
     * @param repositories the repositories list
     * @return this builder
     */
    public ElementDeploymentBuilder repositories(final List<ElementArtifactRepository> repositories) {
        this.repositories = repositories != null ? new ArrayList<>(repositories) : new ArrayList<>();
        return this;
    }

    /**
     * Adds a repository.
     *
     * @param repository the repository
     * @return this builder
     */
    public ElementDeploymentBuilder addRepository(final ElementArtifactRepository repository) {
        this.repositories.add(repository);
        return this;
    }

    /**
     * Sets the deployment state.
     *
     * @param state the deployment state
     * @return this builder
     */
    public ElementDeploymentBuilder state(final ElementDeploymentState state) {
        this.state = state;
        return this;
    }

    /**
     * Sets the version.
     *
     * @param version the version
     * @return this builder
     */
    public ElementDeploymentBuilder version(final long version) {
        this.version = version;
        return this;
    }

    /**
     * Builds the ElementDeployment instance.
     *
     * @return a new ElementDeployment
     */
    public ElementDeployment build() {
        return new ElementDeployment(
                id,
                application,
                elm,
                pathSpiClassPaths.isEmpty() ? null : pathSpiClassPaths,
                pathAttributes.isEmpty() ? null : pathAttributes,
                elements.isEmpty() ? null : elements,
                packages.isEmpty() ? null : packages,
                useDefaultRepositories,
                repositories.isEmpty() ? null : repositories,
                state,
                version
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
         * Creates a standalone builder for ElementPathDefinition.
         *
         * @return a new ElementPathDefinitionBuilder
         */
        public static ElementPathDefinitionBuilder<Void> builder() {
            return new ElementPathDefinitionBuilder<>(null, def -> {});
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
        private Map<String, List<String>> pathSpiClassPaths = new HashMap<>();
        private Map<String, Map<String, Object>> pathAttributes = new HashMap<>();

        private ElementPackageDefinitionBuilder(
                final ParentT parent,
                final Consumer<ElementPackageDefinition> consumer) {
            this.parent = parent;
            this.consumer = consumer;
        }

        /**
         * Creates a standalone builder for ElementPackageDefinition.
         *
         * @return a new ElementPackageDefinitionBuilder
         */
        public static ElementPackageDefinitionBuilder<Void> builder() {
            return new ElementPackageDefinitionBuilder<>(null, def -> {});
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

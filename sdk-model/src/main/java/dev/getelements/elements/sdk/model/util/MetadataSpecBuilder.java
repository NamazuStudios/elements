package dev.getelements.elements.sdk.model.util;

import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.schema.MetadataSpecProperty;
import dev.getelements.elements.sdk.model.schema.MetadataSpecPropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Builder for constructing {@link MetadataSpec} instances fluently. */
public class MetadataSpecBuilder {

    private final MetadataSpec spec;

    /**
     * Creates a new {@link PropertiesBuilder} for building a standalone list of properties.
     *
     * @return a new properties builder
     */
    public static PropertiesBuilder<List<MetadataSpecProperty>> propertiesBuilder() {
        final var properties = new ArrayList<MetadataSpecProperty>();
        return new PropertiesBuilder<>(properties, properties::addAll);
    }

    /**
     * Creates a new {@link MetadataSpecBuilder} wrapping an existing spec.
     *
     * @param spec the existing spec to wrap
     * @return a new builder
     */
    public static MetadataSpecBuilder with(final MetadataSpec spec) {
        return new MetadataSpecBuilder(spec);
    }

    /** Creates a new instance with a new empty {@link MetadataSpec}. */
    public MetadataSpecBuilder() {
        this(new MetadataSpec());
    }

    /**
     * Creates a new instance wrapping the given spec.
     *
     * @param spec the spec to build on
     */
    public MetadataSpecBuilder(final MetadataSpec spec) {
        this.spec = spec;
    }

    /**
     * Sets the name on the spec.
     *
     * @param name the name
     * @return this builder
     */
    public MetadataSpecBuilder name(final String name) {
        spec.setName(name);
        return this;
    }

    /**
     * Sets the type on the spec.
     *
     * @param type the type
     * @return this builder
     */
    public MetadataSpecBuilder type(final MetadataSpecPropertyType type) {
        spec.setType(type);
        return this;
    }

    /**
     * Sets the type to OBJECT.
     *
     * @return this builder
     */
    public MetadataSpecBuilder objectType() {
        return type(MetadataSpecPropertyType.OBJECT);
    }

    /**
     * Returns the built {@link MetadataSpec}.
     *
     * @return the metadata spec
     */
    public MetadataSpec endMetadataSpec() {
        return spec;
    }

    /**
     * Returns a new {@link PropertiesBuilder} for adding properties to the spec.
     *
     * @return a new properties builder
     */
    public PropertiesBuilder<MetadataSpecBuilder> properties() {
        return new PropertiesBuilder<>(this, spec::setProperties);
    }

    /**
     * Builder for constructing a list of {@link MetadataSpecProperty} instances.
     *
     * @param <ParentT> the parent builder type to return when done
     */
    public static class PropertiesBuilder<ParentT> {

        private final ParentT parent;

        private final List<MetadataSpecProperty> properties = new ArrayList<>();

        private final Consumer<List<MetadataSpecProperty>> propertiesConsumer;

        private PropertiesBuilder(
                final ParentT parent,
                final Consumer<List<MetadataSpecProperty>> propertiesConsumer) {
            this.parent = parent;
            this.propertiesConsumer = propertiesConsumer;
        }

        /**
         * Returns a new {@link PropertyBuilder} for adding a property.
         *
         * @return a new property builder
         */
        public PropertyBuilder<ParentT> property() {
            return new PropertyBuilder<>(this);
        }

        /**
         * Finalizes the properties list and returns the parent builder.
         *
         * @return the parent builder
         */
        public ParentT endProperties() {
            propertiesConsumer.accept(properties);
            return parent;
        }

    }

    /**
     * Builder for constructing a single {@link MetadataSpecProperty}.
     *
     * @param <ParentT> the parent builder type to return when done
     */
    public static class PropertyBuilder<ParentT> {

        private final PropertiesBuilder<ParentT> parent;

        private final MetadataSpecProperty property = new MetadataSpecProperty();

        private final List<MetadataSpecProperty> properties = new ArrayList<>();

        private PropertyBuilder(final PropertiesBuilder<ParentT> parent) {
            this.parent = parent;
        }

        /**
         * Sets the name of the property.
         *
         * @param name the name
         * @return this builder
         */
        public PropertyBuilder<ParentT> name(final String name) {
            property.setName(name);
            return this;
        }

        /**
         * Sets the type of the property.
         *
         * @param metadataSpecPropertyType the type
         * @return this builder
         */
        public PropertyBuilder<ParentT> type(final MetadataSpecPropertyType metadataSpecPropertyType) {
            property.setType(metadataSpecPropertyType);
            return this;
        }

        /**
         * Sets the type to STRING.
         *
         * @return this builder
         */
        public PropertyBuilder<ParentT> stringType() {
            return type(MetadataSpecPropertyType.STRING);
        }

        /**
         * Sets the type to NUMBER.
         *
         * @return this builder
         */
        public PropertyBuilder<ParentT> numberType() {
            return type(MetadataSpecPropertyType.NUMBER);
        }

        /**
         * Sets the display name of the property.
         *
         * @param displayName the display name
         * @return this builder
         */
        public PropertyBuilder<ParentT> displayName(final String displayName) {
            property.setDisplayName(displayName);
            return this;
        }

        /**
         * Sets whether the property is required.
         *
         * @param isRequired true if required
         * @return this builder
         */
        public PropertyBuilder<ParentT> required(final boolean isRequired) {
            property.setRequired(isRequired);
            return this;
        }

        /**
         * Sets the placeholder text for the property.
         *
         * @param placeholder the placeholder
         * @return this builder
         */
        public PropertyBuilder<ParentT> placeholder(final String placeholder) {
            property.setPlaceholder(placeholder);
            return this;
        }

        /**
         * Sets the default value for the property.
         *
         * @param defaultValue the default value
         * @return this builder
         */
        public PropertyBuilder<ParentT> defaultValue(final Object defaultValue) {
            property.setDefaultValue(defaultValue);
            return this;
        }

        /**
         * Returns a new {@link PropertiesBuilder} for adding nested properties.
         *
         * @return a new nested properties builder
         */
        public PropertiesBuilder<PropertyBuilder<ParentT>> properties() {
            return new PropertiesBuilder<>(this, property::setProperties);
        }

        /**
         * Finalizes this property, adds it to the parent list, and returns the parent builder.
         *
         * @return the parent properties builder
         */
        public PropertiesBuilder<ParentT> endProperty() {
            parent.properties.add(property);
            return parent;
        }

    }

}

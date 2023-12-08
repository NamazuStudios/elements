package dev.getelements.elements.util;

import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.MetadataSpecProperty;
import dev.getelements.elements.model.schema.MetadataSpecPropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MetadataSpecBuilder {

    private final MetadataSpec spec;

    public static PropertiesBuilder<List<MetadataSpecProperty>> propertiesBuilder() {
        final var properties = new ArrayList<MetadataSpecProperty>();
        return new PropertiesBuilder<>(properties, properties::addAll);
    }

    public static MetadataSpecBuilder with(final MetadataSpec spec) {
        return new MetadataSpecBuilder(spec);
    }

    public MetadataSpecBuilder() {
        this(new MetadataSpec());
    }

    public MetadataSpecBuilder(final MetadataSpec spec) {
        this.spec = spec;
    }

    public MetadataSpecBuilder name(final String name) {
        spec.setName(name);
        return this;
    }

    public MetadataSpecBuilder type(final MetadataSpecPropertyType type) {
        spec.setType(type);
        return this;
    }

    public MetadataSpecBuilder objectType() {
        return type(MetadataSpecPropertyType.OBJECT);
    }

    public MetadataSpec endMetadataSpec() {
        return spec;
    }

    public PropertiesBuilder<MetadataSpecBuilder> properties() {
        return new PropertiesBuilder<>(this, spec::setProperties);
    }

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

        public PropertyBuilder<ParentT> property() {
            return new PropertyBuilder<>(this);
        }

        public ParentT endProperties() {
            propertiesConsumer.accept(properties);
            return parent;
        }

    }

    public static class PropertyBuilder<ParentT> {

        private final PropertiesBuilder<ParentT> parent;

        private final MetadataSpecProperty property = new MetadataSpecProperty();

        private final List<MetadataSpecProperty> properties = new ArrayList<>();

        private PropertyBuilder(final PropertiesBuilder<ParentT> parent) {
            this.parent = parent;
        }

        public PropertyBuilder<ParentT> name(final String name) {
            property.setName(name);
            return this;
        }

        public PropertyBuilder<ParentT> type(final MetadataSpecPropertyType metadataSpecPropertyType) {
            property.setType(metadataSpecPropertyType);
            return this;
        }

        public PropertyBuilder<ParentT> stringType() {
            return type(MetadataSpecPropertyType.STRING);
        }

        public PropertyBuilder<ParentT> numberType() {
            return type(MetadataSpecPropertyType.NUMBER);
        }

        public PropertyBuilder<ParentT> displayName(final String displayName) {
            property.setDisplayName(displayName);
            return this;
        }

        public PropertyBuilder<ParentT> required(final boolean isRequired) {
            property.setRequired(isRequired);
            return this;
        }

        public PropertyBuilder<ParentT> placeholder(final String placeholder) {
            property.setPlaceholder(placeholder);
            return this;
        }

        public PropertyBuilder<ParentT> defaultValue(final Object defaultValue) {
            property.setDefaultValue(defaultValue);
            return this;
        }

        public PropertiesBuilder<PropertyBuilder<ParentT>> properties() {
            return new PropertiesBuilder<>(this, property::setProperties);
        }

        public PropertiesBuilder<ParentT> endProperty() {
            parent.properties.add(property);
            return parent;
        }

    }

}

package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.MutableAttributes;

import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An instance of {@link Attributes} backed by a {@link Properties} instance.
 */
public class PropertiesAttributes implements Attributes, MutableAttributes {

    /**
     * Takes a copy of the supplied {@link Attributes} and returns a new {@link PropertiesAttributes}.
     *
     * @param attributes the {@link Attributes} to copy
     * @return the {@link PropertiesAttributes}
     */
    public static PropertiesAttributes copyOf(Attributes attributes) {
        return new PropertiesAttributes(attributes.asProperties());
    }

    /**
     * Wraps the provided {@link Properties} in an instance of {@link MutableAttributes}. Changes to the
     * underlying {@link Properties} will be visible.
     *
     * @param properties the {@link Properties} to wrap
     * @return a wrapped instance of {@link MutableAttributes}
     */
    public static PropertiesAttributes wrap(final Properties properties) {
        return new PropertiesAttributes(properties);
    }

    private final Properties properties;

    private PropertiesAttributes(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public Set<String> getAttributeNames() {
        return properties.stringPropertyNames();
    }

    @Override
    public Stream<Attribute<Object>> stream() {
        return properties
                .stringPropertyNames()
                .stream()
                .map(key -> new Attribute<>(key, properties.getProperty(key)));
    }

    @Override
    public Optional<Object> getAttributeOptional(final String name) {
        return Optional.ofNullable(properties.get(name));
    }

    @Override
    public void setAttribute(String name, Object obj) {
        properties.put(name, obj);
    }

    @Override
    public Attributes immutableCopy() {
        return ImmutableAttributes.copyOf(this);
    }

}

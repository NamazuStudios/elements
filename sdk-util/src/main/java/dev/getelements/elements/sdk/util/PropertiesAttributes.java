package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.MutableAttributes;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableSet;

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
        return properties.keySet()
                .stream()
                .map(Object::toString)
                .collect(toUnmodifiableSet());
    }

    @Override
    public Stream<Attribute<Object>> stream() {
        return properties
                .entrySet()
                .stream()
                .map(e -> new Attribute<>(e.getKey().toString(), e.getValue()));
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

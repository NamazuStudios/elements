package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.MutableAttributes;

import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class PropertiesAttributes implements Attributes, MutableAttributes {

    private final Properties properties;

    public PropertiesAttributes(final Properties properties) {
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

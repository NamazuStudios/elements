package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Attributes;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;

/**
 * A type of {@link Attributes} which is immutable.
 */
public class ImmutableAttributes implements Attributes {

    /**
     * Provides an immutable view of the {@link Map} containing the attributes.
     *
     * @param attributes a {@link Map} from which to provide the view of the attributes
     *
     * @return an immutable view of the {@link Attributes}
     */
    public static ImmutableAttributes viewOf(final Map<String, Object> attributes) {
        final var unmodifiableView = unmodifiableMap(attributes);
        return new ImmutableAttributes(unmodifiableView);
    }

    /**
     * Takes a copy of the supplied {@link Attributes} and returns the result.
     *
     * @param attributes the {@link Attributes} from which to copy this.
     *
     * @return a copy of the {@link Attributes}
     */
    public static ImmutableAttributes copyOf(final Attributes attributes) {
        final var copy = Map.copyOf(attributes.asMap());
        return new ImmutableAttributes(copy);
    }

    @Override
    public Attributes immutableCopy() {
        return this;
    }

    private final Map<String, Object> attributes;

    private ImmutableAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    @Override
    public Optional<Object> getAttributeOptional(final String name) {
        final var attribute = attributes.get(name);
        return Optional.ofNullable(attribute);
    }

    @Override
    public Map<String, Object> asMap() {
        return attributes;
    }

    @Override
    public Stream<Attribute<Object>> stream() {
        return attributes
                .entrySet()
                .stream()
                .map(e -> new Attribute<>(e.getKey(), e.getValue()));
    }

    @Override
    public boolean equals(Object o) {
        return Attributes.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Attributes.hashCode(this);
    }

}

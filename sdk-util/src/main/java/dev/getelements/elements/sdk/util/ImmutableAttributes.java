package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Attributes;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A type of {@link Attributes} which is immutable.
 */
public class ImmutableAttributes implements Attributes {

    /**
     * Takes a copy of the supplied {@link Attributes} and then
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

}

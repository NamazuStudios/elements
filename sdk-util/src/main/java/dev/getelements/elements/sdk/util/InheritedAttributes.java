package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.MutableAttributes;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * A type of {@link Attributes} that inherits from a base set of attributes.
 *
 * @param base the base attributes, may be null and will be replaced with {@link Attributes#emptyAttributes()}
 * @param current the current attributes that are inherited from the base attributes
 */
public record InheritedAttributes(Attributes base, Attributes current) implements Attributes {

    public InheritedAttributes {

        if (base == null) {
            base = Attributes.emptyAttributes();
        }

        if (current == null) {
            throw new IllegalArgumentException("current attributes cannot be null");
        }

    }

    /**
     * Creates an {@link InheritedAttributes} object that inherits from the given attributes.
     *
     * @param current the attributes to inherit from
     * @return the new {@link InheritedAttributes}
     */
    public static InheritedAttributes withAttributes(Attributes current) {
        return new InheritedAttributes(null, current);
    }

    @Override
    public Set<String> getAttributeNames() {
        final Set<String> result = new TreeSet<>();
        result.addAll(base().getAttributeNames());
        result.addAll(current().getAttributeNames());
        return result;
    }

    @Override
    public Optional<Object> getAttributeOptional(String name) {
        return current().getAttributeOptional(name).or(() -> base().getAttributeOptional(name));
    }

    @Override
    public Attributes immutableCopy() {
        return ImmutableAttributes.copyOf(this);
    }

    /**
     * Derives a new set of attributes from the current set of attributes.
     * @param attributes the attributes
     * @return the new {@link InheritedAttributes}
     */
    public InheritedAttributes newDerivativeFrom(final Attributes attributes) {
        return new InheritedAttributes(this, attributes);
    }

    /**
     * Derives a new set of attributes from the current set of attributes.
     * @param attributes the attributes
     * @return the new {@link InheritedAttributes}
     */
    public InheritedMutableAttributes newDerivativeFrom(final MutableAttributes attributes) {
        return new InheritedMutableAttributes(this, attributes);
    }

}

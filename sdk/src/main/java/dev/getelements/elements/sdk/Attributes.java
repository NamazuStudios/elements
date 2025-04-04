package dev.getelements.elements.sdk;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;

/**
 * Contains attributes which represent contextual information for various types.
 *
 * The test for {@link Object#equals(Object)} and {@link Object#hashCode()} should be implemented using the provided
 * static methods in this interface, or an equivalent algorithm. Many {@link Attributes} implementations exist and the
 * most reliable means must be determined by the contents as they are accessible through this interface and not in an
 * implementation specific way.  Failure to do so may result in poor cache performance as {@link Attributes} may be a
 * component of a cache key in some parts of the application.
 */
public interface Attributes {

    /**
     * Gets a {@link List<String>} containing all attribute names contained in this instance.
     *
     * @return the {@link List<String>} of attribute names
     */
    Set<String> getAttributeNames();

    /**
     * Gets all {@link Attribute}s as a {@link Stream}.
     *
     * @return a {@link Stream} of {@link Attribute}s
     */
    default Stream<Attribute<Object>> stream() {
        return getAttributeNames()
                .stream()
                .map(name -> new Attribute<>(name, getAttribute(name)));
    }

    /**
     * Gets the attribute associated with this {@link Attributes} for the given name.  Returning null if no such
     * attribute is found.  Note that an attribute may exist with the supplied name and the value null.  In order to
     * distinguish from this, the method {@link #getAttributeOptional(String)} may be used.
     *
     * @param name the name of hte attribute
     * @return the value or null
     */
    default Object getAttribute(final String name) {
        final Optional<Object> optionalAttribute = getAttributeOptional(name);
        return optionalAttribute.orElse(null);
    }

    /**
     * Gets the attribute associated with this {@link Attributes} object.
     *
     * @param name the name of the attribute to fetch
     *
     * @return an {@link Optional<Object>} for the value
     */
    Optional<Object> getAttributeOptional(String name);

    /**
     * Returns a copy of this {@link Attributes} as an immutable copy. Alternatively, if this instance is already
     * immutable, then this will return this instance.
     *
     * @return an immutable copy of this {@link Attributes}
     */
    Attributes immutableCopy();

    /**
     * Returns a view of this {@link Attributes} object as a {@link Map<String, Object>}.
     *
     * @return this {@link Attributes} as a {@link Map<String, Object>}
     */
    default Map<String, Object> asMap() {
        return getAttributeNames()
            .stream()
            .collect(toMap(name -> name, this::getAttribute));
    }

    /**
     * Returns this {@link Attributes} as a {@link Properties} instance, copying all attributes over.
     *
     * @return the {@link Properties}
     */
    default Properties asProperties() {
        final var properties = new Properties();
        stream().forEach(attribute -> properties.put(attribute.name(), attribute.value()));
        return properties;
    }

    /**
     * Returns this {@link Attributes} as a {@link Properties} instance.
     *
     * @param defaults the default properties to use
     * @return the {@link Properties}
     */
    default Properties asProperties(final Properties defaults) {
        final var properties = new Properties(defaults);
        stream().forEach(attribute -> properties.put(attribute.name(), attribute.value()));
        return properties;
    }

    /**
     * The empty {@link Attributes} implementation.  This returns an empty list of attribute names, and will return null
     * for any requested attribute.
     */
    Attributes EMPTY = new EmptyAttributes();

    /**
     * Returns {@link #EMPTY}.
     *
     * @return {@link #EMPTY}
     */
    static Attributes emptyAttributes() {
        return EMPTY;
    }

    /**
     * Given a set of {@link Attributes}, this will determined is hash code.  This must be consistent with the rules of
     * {@link Object#hashCode()} and {@link Attributes#equals(Attributes, Attributes)}
     *
     * @param attributes the {@link Attributes}
     * @return the hash code
     */
    static int hashCode(final Attributes attributes) {
        return attributes.asMap().hashCode();
    }

    /**
     * Test if the supplied {@link Attributes} is equal to the other {@link Object} by first testing if it an instance
     * of {@link Attributes} as well as if {@link #equals(Attributes, Attributes)} returns true.
     *
     * @param a the attributes object
     * @param b another obect
     * @return true if equal, false otherwise.
     */
    static boolean equals(final Attributes a, final Object b) {
        return (b instanceof Attributes) && Attributes.equals(a, b);
    }

    /**
     * Tests if two {@link Attributes} are equal to each other.  This is used by subclasses toprovide a universal test
     * for equality against another instance of {@link Attributes}.
     *
     * Generally equality is defined as follows:
     *
     * <p><ul>
     * <li>The names of all attributes are the same (order notwithstanding)</li>
     * <li>The name set of all attributes are equal in size.</li>
     * <li>Each individual attribute is equal (as determined by {@link Object#equals(Object)}</li>
     * </ul><p>
     *
     *
     * @param a the first {@link Attributes}
     * @param b the second {@link Attributes}
     *
     * @return true if equals, false otherwise.
     */
    static boolean equals(final Attributes a, final Attributes b) {
        return a.asMap().equals(b.asMap());
    }

    /**
     * Represents a single {@link Attribute}.
     *
     * @param name the name
     * @param value the value
     * @param <T> the type
     */
    record Attribute<T>(String name, T value) {

        /**
         * Gets this Attribute as a subtype, throwing a {@link ClassCastException} if the attribute is incompatible.
         *
         * @param uClass the type to cast
         * @return the Attribute
         * @param <U> the desired type
         */
        <U extends T> Attribute<U> as(final Class<U> uClass) {
            return new Attribute<>(uClass.getName(), uClass.cast(value()));
        }

    }

}

class EmptyAttributes implements Attributes, Serializable {

    @Override
    public Set<String> getAttributeNames() {
        return emptySet();
    }

    @Override
    public Stream<Attribute<Object>> stream() {
        return Stream.empty();
    }

    @Override
    public Map<String, Object> asMap() {
        return emptyMap();
    }

    @Override
    public Optional<Object> getAttributeOptional(String name) {
        return empty();
    }

    @Override
    public int hashCode() {
        return Attributes.hashCode(this);
    }

    @Override
    public boolean equals(final Object that) {
        return Attributes.equals(this, that);
    }

    @Override
    public Attributes immutableCopy() {
        return this;
    }

}

package dev.getelements.elements.sdk.util;


import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.MutableAttributes;
import dev.getelements.elements.sdk.record.ElementDefaultAttributeRecord;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

import static java.util.Collections.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Simple implementation of {@link Attributes} backed by a {@link Map<String, Object>}.
 */
public record SimpleAttributes(Map<String, Object> attributes) implements MutableAttributes, Serializable {

    @Override
    public Set<String> getAttributeNames() {
        return attributes() == null ? emptySet() : unmodifiableSet(attributes().keySet());
    }

    @Override
    public Optional<Object> getAttributeOptional(final String name) {

        final Map<String, Object> attributes = attributes();
        if (attributes == null) return Optional.empty();

        final Object object = attributes.get(name);
        return object == null ? empty() : of(object);

    }

    @Override
    public Map<String, Object> asMap() {
        return attributes;
    }

    @Override
    public void setAttribute(final String name, final Object obj) {
        attributes.put(name, obj);
    }

    @Override
    public Attributes immutableCopy() {
        return ImmutableAttributes.copyOf(this);
    }

    @Override
    public int hashCode() {
        return Attributes.hashCode(this);
    }

    @Override
    public boolean equals(final Object that) {
        return Attributes.equals(this, that);
    }

    /**
     * Constructs a new simple attributes instance with the default backing.
     * @return a new {@link SimpleAttributes}
     */
    public static SimpleAttributes newDefaultInstance() {
        return new SimpleAttributes(new LinkedHashMap<>());
    }

    /**
     * Builder for {@link SimpleAttributes}.
     */
    public static class Builder {

        private final Map<String, Object> attributes = new LinkedHashMap<>();

        /**
         * Bulk-sets attributes using the supplied {@link Map<String , Object>}. All supplied attributes will be added
         * and existing keys overwritten with the values present in the supplied map.
         * <p>
         * Passing null will have no effect.
         *
         * @param attributes the map of attributes, or null.
         * @return this instance
         */
        public Builder setAttributes(final Map<String, Object> attributes) {
            this.attributes.putAll(attributes == null ? emptyMap() : attributes);
            return this;
        }

        /**
         * Sets the provided attribute using the name and value.
         *
         * @param name  the attribute name
         * @param value the attribute value
         * @return this instance
         */
        public Builder setAttribute(final String name, final Object value) {
            attributes.put(name, value);
            return this;
        }

        /**
         * Copies all values from another {@link Attributes} into this {@link Builder}.
         *
         * @param attributes the {@link Attributes}
         * @return this instance
         */
        public Builder from(final Attributes attributes) {

            for (final String name : attributes.getAttributeNames()) {
                setAttribute(name, attributes.getAttribute(name));
            }

            return this;

        }

        /**
         * Copies all values from another {@link Attributes} into this {@link Builder}, filtering out attributes
         * based on the supplied {@link BiPredicate}.
         *
         * @param attributes the {@link Attributes}
         * @param filter     a {@link BiPredicate<String, Object>} used to select specific entries to copy
         * @return this instance
         */
        public Builder from(final Attributes attributes, final BiPredicate<String, Object> filter) {

            for (final String name : attributes.getAttributeNames()) {
                final Object value = attributes.getAttributeOptional(name);
                if (filter.test(name, value)) setAttribute(name, attributes.getAttributeOptional(name));
            }

            return this;

        }

        /**
         * Copies all values from another {@link Attributes} into this {@link Builder}.
         *
         * @param defaultAttributeRecords a collection of {@link ElementDefaultAttributeRecord}
         * @return this instance
         */
        public Builder from(final Iterable<ElementDefaultAttributeRecord> defaultAttributeRecords) {

            for (final var defaultRecord : defaultAttributeRecords) {
                setAttribute(defaultRecord.name(), defaultRecord.value());
            }

            return this;

        }

        /**
         * Returns a new instance of {@link SimpleAttributes} based on this builder.
         *
         * @return a new instance of {@link SimpleAttributes}
         */
        public SimpleAttributes build() {
            final var attributes = new LinkedHashMap<>(this.attributes);
            return new SimpleAttributes(attributes);
        }

    }

}

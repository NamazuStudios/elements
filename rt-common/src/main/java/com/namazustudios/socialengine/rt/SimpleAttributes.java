package com.namazustudios.socialengine.rt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

/**
 * Simple implementation of {@link Attributes} backed by a {@link Map<String, Object>}.
 */
public class SimpleAttributes implements Attributes {

    private Map<String, Object> attributes;

    @Override
    public Set<String> getAttributeNames() {
        return getAttributes() == null ? emptySet() : unmodifiableSet(getAttributes().keySet());
    }

    @Override
    public Object getAttribute(String name) {
        return getAttributes().get(name);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Builder for {@link SimpleAttributes}.
     */
    public static class Builder {

        private final Map<String, Object> attributes = new LinkedHashMap<>();

        /**
         * Bulk-sets attributes using the supplied {@link Map<String , Object>}.
         *
         * @param attributes the map of attributes
         * @return this instance
         */
        public Builder setAttributes(final Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        /**
         * Sets the provided attribute using the name and value.
         *
         * @param name the attribute name
         * @param value the attribute value
         * @return this instance
         */
        public Builder setAttribute(final String name, final Object value) {
            attributes.put(name, value);
            return this;
        }

        /**
         * Returns a new instance of {@link SimpleAttributes} based on this builder.
         *
         * @return a new instance of {@link SimpleAttributes}
         */
        public SimpleAttributes build() {
            final SimpleAttributes simpleAttributes = new SimpleAttributes();
            simpleAttributes.setAttributes(new LinkedHashMap<>(attributes));
            return simpleAttributes;
        }

    }

}

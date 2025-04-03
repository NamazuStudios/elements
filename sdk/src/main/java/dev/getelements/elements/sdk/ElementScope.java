package dev.getelements.elements.sdk;

/**
 * Associates a set of {@link Attributes} with a transient state for the {@link Element}.
 */
public interface ElementScope extends AutoCloseable {

    /**
     * Gets the Attributes associated with this scope.
     *
     * @return the {@link Attributes} associated with the scope
     */
    Attributes getAttributes();

    /**
     * Closes this {@link ElementScope}
     */
    void close();

    /**
     * Builds the specific instances to go into the scope.
     */
    interface Builder {

        /**
         * Adds an object to the {@link Attributes} attached to the {@link ElementScope}.
         *
         * @param object the object to bind
         * @param name the name of the object to bin d
         * @return this instance
         * @param <T>
         */
        <T> Builder with(String name, T object);

        /**
         * Copies the {@link Attributes} to the {@link ElementScope}.
         *
         * @param attributes the attributes
         * @return this instance
         */
        default Builder with(final Attributes attributes) {
            attributes.attributesStream().forEach(attribute -> with(attribute.name(), attribute.value()));
            return this;
        }

        /**
         * Builds the {@link ElementScope}.
         *
         * @return the {@link ElementScope}
         */
        ElementScope build();

    }

}

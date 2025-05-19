package dev.getelements.elements.sdk;

/**
 * Associates a set of {@link Attributes} with a transient state for the {@link Element}. The Element
 * retains this scope for the current thread until the scope is closed. Opening new scopes will inherit and
 * override the variables set in the associated {@link MutableAttributes}.
 */
public interface ElementScope {

    /**
     * The default name of an {@link ElementScope}
     */
    String ANONYMOUS = "<anonymous>";

    /**
     * Gets the name of the scope.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the Attributes associated with this scope.
     *
     * @return the {@link Attributes} associated with the scope
     */
    MutableAttributes getMutableAttributes();

    /**
     * Builds the specific instances to go into the scope.
     */
    interface Builder {

        /**
         * The name of the scope. The name is meant to assist in debugging and other tasks
         *
         * @param name the name of the scope
         * @return this instance
         */
        Builder named(String name);

        /**
         * Sets the {@link Attributes} to the scope.
         *
         * @param attributes the attributes to set
         * @return this instance
         */
        Builder with(final Attributes attributes);

        /**
         * Builds the {@link ElementScope}.
         *
         * @return the {@link ElementScope}
         */
        Handle enter();

    }

    /**
     * Represents an open handle for an open {@link ElementScope} on the current thread.
     */
    @FunctionalInterface
    interface Handle extends AutoCloseable {

        /**
         * Releases the current scope for this thread.
         */
        void close();

    }

}

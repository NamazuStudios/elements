package dev.getelements.elements.sdk;

public interface ElementScope extends AutoCloseable {

    /**
     * Gets the object associated with the scoped type.
     *
     * @param clazz the scoped type
     * @return the object
     * @param <T>
     */
    <T> T get(Class<T> clazz);

    /**
     * Closes this {@link ElementScope}
     */
    void close();

    /**
     * Builds the specific instances to go into the scope.
     */
    interface Builder {

        <T> Builder with(T object);

        <T> Builder with(T object, String name);

        ElementScope build();

    }

}

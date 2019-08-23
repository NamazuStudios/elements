package com.namazustudios.socialengine.rt;

/**
 * Implements a round-robin data structure which will repeatedly select from a set of values ensuring an even
 * distribution across all values.
 *
 * @param <T>
 */
public interface RoundRobin<T> {

    /**
     * Gets the next object in this {@link RoundRobin}
     *
     * @return the next object
     */
    T getNext();

    /**
     * Sets the objects at the supplied index.
     *
     * @param index the index
     * @param object the object
     */
    T set(int index, T object);

    /**
     * Gets the specific object at that index.
     *
     * @param index the index
     *
     * @return the object
     */
    T get(int index);
}

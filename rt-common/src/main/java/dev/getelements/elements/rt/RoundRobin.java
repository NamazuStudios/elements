package dev.getelements.elements.rt;

import java.util.stream.Stream;

/**
 * Implements a round-robin data structure which will repeatedly select from a set of values ensuring an even
 * distribution across all values.
 *
 * @param <T>
 */
public interface RoundRobin<T> extends Iterable<T> {

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

    /**
     * Gets a {@link Stream<T>} from this {@link RoundRobin<T>}.
     *
     * @return the {@link Stream<T>} of all elements
     */
    Stream<T> stream();
}

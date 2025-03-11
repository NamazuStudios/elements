package dev.getelements.elements.sdk.util;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents a Lazy-loaded value which is loaded only on the first call to get. Though it is not a functional
 * interface, it does extend {@link Supplier} so it can easily drop-in directly to various standard APIs.
 *
 * The interface makes no gaurantees as to thead safety, however specific implementations may.
 *
 * @param <T> the type to supply
 */
public interface LazyValue<T> extends Supplier<T> {

    /**
     * Gets the value of this {@link SimpleLazyValue}, computing it if it was not already computed.
     *
     * @return the optional value
     */
    T get();

    /**
     * Gets an {@link Optional} representing this {@link SimpleLazyValue}. If the value was not previously loaded,
     * then this simply returns an empty.
     *
     * @return the {@link Optional} representing the value
     */
    Optional<T> getOptional();

}

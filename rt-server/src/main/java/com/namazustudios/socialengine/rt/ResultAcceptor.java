package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InvalidConversionException;

import java.util.function.Consumer;

/**
 * Provides a means to accept results returned by instances of {@link MethodDispatcher}
 */
public interface ResultAcceptor<T> {

    /**
     * Calling calling this method completes the dispatch tho th underlying {@link Resource}'s method and will
     * place the response of the method call into the provided {@link Consumer<T>}.
     *
     * @param tConsumer the consumer
     */
    void dispatch(Consumer<T> tConsumer, Consumer<Throwable> throwableTConsumer);

    /**
     * Returns a {@link ResultAcceptor} for the specified type.  The default implementation
     * simply attempts to cast the result type to the desired type and performs no other
     * conversion.
     *
     * @param uClass the desired type
     * @param <U> the desired type
     */
    default <U> ResultAcceptor<U> forResultType(final Class<U> uClass) {
        return (uConsumer, thConsumer) -> dispatch(t -> {
            try {
                uConsumer.accept(uClass.cast(t));
            } catch (ClassCastException ex) {
                throw new InvalidConversionException(ex);
            }
        }, thConsumer);
    }

}

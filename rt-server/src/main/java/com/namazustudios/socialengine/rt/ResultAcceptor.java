package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InvalidConversionException;

import java.util.function.Consumer;

/**
 * Provides a means to accept results returned by instances of {@link MethodDispatcher}
 */
public interface ResultAcceptor<T> {

    /**
     *
     *
     * @param tConsumer
     */
    void withConsumer(Consumer<T> tConsumer);

    /**
     * Returns a {@link ResultAcceptor} for the specified type.  The default implementation
     * simply attempts to cast the result type to the desired type and performs no other
     * conversion.
     *
     * @param uClass the desired type
     * @param <U> the desired type
     */
    default <U> ResultAcceptor<U> forResultType(final Class<U> uClass) {
        return uConsumer -> {
            withConsumer(t -> {
                try {
                    uConsumer.accept(uClass.cast(t));
                } catch (ClassCastException ex) {
                    throw new InvalidConversionException(ex);
                }
            });
        };
    }

}

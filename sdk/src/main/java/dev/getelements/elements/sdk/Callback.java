package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkException;

/**
 * Indicates a Callback type. A {@link Callback} is a reference to a specific addressable object within an
 * {@link ElementRegistry}. This is a method call, expecting a result, from a specific service within an
 * {@link Element}.
 *
 * @param <ResultT> the result type
 */
public interface Callback<ResultT> {

    /**
     * Executes the callback against the resolved service.
     *
     * @param args the arguments
     * @return the call to make
     */
    ResultT call(Object ... args);

    /**
     * Makes a new {@link Callback} which safely returns the requested type.
     *
     * @param newResultTClass the new result type {@link Class}
     * @return the new {@link Callback} as the specified type
     * @param <NewResultT> the new result type
     * @throws SdkException if there is a missing callback type
     */
    default <NewResultT> Callback<NewResultT> as(final Class<NewResultT> newResultTClass) {
        return args -> newResultTClass.cast(call(args));
    }

}

package com.namazustudios.socialengine.rt;

/**
 * Generates an instance of {@link Response} as the result of any user code throwing an exception.  If no exception
 * handler is available to catch the exception, then the server container handles the exception with default behavior.
 *
 *
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface ExceptionMapper<ExceptionT extends Throwable> {

    /**
     * Maps the given {@link Exception} to a custom {@link Response} and then supplies the response to the given
     * {@link ResponseReceiver}.
     *
     * It is not recommended that this method throw an exception, but rather log and write the appropriate
     * exception type to the {@link ResponseReceiver}.
     *
     * @param exception the exception the exception
     * @param responseReceiver the response the response
     *
     */
    void map(ExceptionT exception, ResponseReceiver responseReceiver);

    /**
     * Performs the same task as {@link #map(Throwable, ResponseReceiver)}, except that it may
     * provide a more enhanced mapping by accepting the {@link Request} instance that caused the
     * exception.
     *
     * The default implementation simply elides the {@link Request} and hands the rest on to
     * it's sister method.
     *
     * @param exception the exception the exception
     * @param request the request the request
     * @param responseReceiver the response the response
     *
     */
    default void map(final ExceptionT exception, final Request request, final ResponseReceiver responseReceiver) {
        map(exception, responseReceiver);
    }

    /**
     * Resolves an {@link ExceptionMapper} for hte given {@link Exception} type.
     */
    interface Resolver {

        /**
         * Returns and {@link ExceptionMapper} for the given {@link Exception} type.  If the type
         * cannot be mapped by a user-defined {@link ExceptionMapper}, then this must return the
         * {@link DefaultExceptionMapper} instance as obtained by {@link DefaultExceptionMapper#getInstance()}.
         *
         * Under no circumstances is it appropriate to return null.
         *
         * @param ex the exception itself
         * @param <ExceptionT> the type of exception
         *
         * @return the {@link ExceptionMapper}, never null
         */
        <ExceptionT extends Throwable> ExceptionMapper<ExceptionT> getExceptionMapper(ExceptionT ex);

    }

}

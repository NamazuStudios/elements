package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

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
     * {@link Consumer <Response>}.
     *
     * It is not recommended that this method throw an exception, but rather log and write the appropriate
     * exception type to the {@link Consumer<Response>}.
     *
     * @param exception the exception the exception
     * @param responseConsumer the response the response
     *
     */
    void map(ExceptionT exception, Consumer<Response> responseConsumer);

    /**
     * Performs the same task as {@link #map(Throwable, Consumer<Response>)}, except that it may
     * provide a more enhanced mapping by accepting the {@link Request} instance that caused the
     * exception.
     *
     * The default implementation simply elides the {@link Request} and hands the rest on to
     * it's sister method.
     *
     * @param exception the exception the exception
     * @param request the request the request
     * @param responseConsumer the response the response
     *
     */
    default void map(final ExceptionT exception, final Request request, final Consumer<Response> responseConsumer) {
        map(exception, responseConsumer);
    }

    /**
     * Resolves an {@link ExceptionMapper} for hte given {@link Exception} type.
     */
    interface Resolver {

        /**
         * Returns and {@link ExceptionMapper} for the given {@link Exception} type.  If the type cannot be mapped by a
         * user-defined {@link ExceptionMapper}, then this must return the {@link DefaultExceptionMapper} instance as
         * obtained by {@link DefaultExceptionMapper#getInstance()}.
         *
         * Under no circumstances is it appropriate to return null.
         *
         * @param ex the exception itself
         * @param <ExceptionT> the type of exception
         *
         * @return the {@link ExceptionMapper}, never null
         */
        <ExceptionT extends Throwable> ExceptionMapper<ExceptionT> getExceptionMapper(ExceptionT ex);

        /**
         * Performs the provided {@link Runnable} within a try/catch passing any caught exceptions to the appropriate
         * {@link ExceptionMapper<?>}
         *
         * @param responseConsumer the response consumer
         * @param runnable the operation to perform
         */
        default void performExceptionSafe(final Consumer<Response> responseConsumer,
                                          final ProtectedOperation runnable) {
            try {
                runnable.perform();
            } catch (Throwable ex) {
                getExceptionMapper(ex).map(ex, responseConsumer);
            }
        }

        /**
         * Performs the provided {@link Runnable} within a try/catch.
         *
         * @param request the request
         * @param responseConsumer the response consumer
         * @param runnable the operation to perform
         */
        default ProtectedOperation performExceptionSafe(final Request request,
                                                        final Consumer<Response> responseConsumer,
                                                        final ProtectedOperation runnable) {
            return () -> {
                try {
                    runnable.perform();
                } catch (Exception ex) {
                    getExceptionMapper(ex).map(ex, request, responseConsumer);
                }
            };
        }

    }

    @FunctionalInterface
    interface ProtectedOperation {

        void perform() throws Exception;

    }

}

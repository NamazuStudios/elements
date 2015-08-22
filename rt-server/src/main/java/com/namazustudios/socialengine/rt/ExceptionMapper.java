package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.edge.EdgeClient;

/**
 * Generates an instance of {@link Response} as the result
 * of any user code throwing an exception.  If no exception
 * handler is available to catch the exception, then the
 * server container handles the exception with default behavior.
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface ExceptionMapper<ExceptionT extends Exception> {

    /**
     * Maps the given {@link Exception} to a custom payload.
     *
     * @param exception the exception
     * @param request the request
     * @param responseReceiver the response
     *
     */
    void map(ExceptionT exception, EdgeClient edgeClient, Request request, ResponseReceiver responseReceiver);

    /**
     * Resolves an {@link ExceptionMapper} for hte given {@link Exception} type.
     */
    interface Resolver {

        /**
         * Returns and {@link ExceptionMapper} for the given {@link Exception} type.
         *
         * @param ex the exception itself
         * @param <ExceptionT> the type of exception
         *
         * @return the {@link ExceptionMapper}
         */
        <ExceptionT extends Exception>
        ExceptionMapper<ExceptionT> getExceptionMapper(ExceptionT ex);

    }

}

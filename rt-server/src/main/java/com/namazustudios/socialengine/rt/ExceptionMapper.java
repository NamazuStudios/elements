package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.edge.EdgeResponseReceiver;

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
     * @param edgeResponseReceiver the response
     *
     */
    void map(ExceptionT exception, Client client, Request request, EdgeResponseReceiver edgeResponseReceiver);

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

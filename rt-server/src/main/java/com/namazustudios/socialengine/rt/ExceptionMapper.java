package com.namazustudios.socialengine.rt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.namazustudios.socialengine.exception.ErrorCode;
import com.namazustudios.socialengine.rt.edge.EdgeClient;

import java.util.Map;

/**
 * Generates an instance of {@link Response} as the result
 * of any user code throwing an exception.  If no exception
 * handler is available to catch the exception, then the
 * server container handles the exception with default behavior.
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface ExceptionMapper<ExceptionT extends Throwable> {

    /**
     * Maps the given {@link Exception} to a custom {@link Response} and then supplies
     * the response to the given {@link ResponseReceiver}.  This method must not throw
     * an exception, even if delivering the response failed.
     *
     * @param exception the exception the exception
     * @param request the request the request
     * @param responseReceiver the response the response
     *
     */
    void map(ExceptionT exception, Request request, ResponseReceiver responseReceiver);

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

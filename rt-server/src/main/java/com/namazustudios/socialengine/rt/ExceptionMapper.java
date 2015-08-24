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
public interface ExceptionMapper<ExceptionT extends Exception> {

    /**
     * Maps the given {@link Exception} to a custom payload.
     *  @param exception the exception
     * @param request the request
     * @param responseReceiver the response
     *
     */
    void map(ExceptionT exception, Request request, ResponseReceiver responseReceiver);

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

    /**
     * Maps the {@link ErrorCode} enum to the {@link ResponseCode} type.
     */
    Map<ErrorCode, ResponseCode> RESPONSE_STATUS_MAP = Maps.immutableEnumMap(
        new ImmutableMap.Builder<ErrorCode, ResponseCode>()
                .put(ErrorCode.DUPLICATE, ResponseCode.BAD_REQUEST_FATAL)
                .put(ErrorCode.FORBIDDEN, ResponseCode.FAILED_AUTH_FATAL)
                .put(ErrorCode.INVALID_DATA, ResponseCode.BAD_REQUEST_FATAL)
                .put(ErrorCode.NOT_FOUND, ResponseCode.PATH_NOT_FOUND)
                .put(ErrorCode.OVERLOAD, ResponseCode.TOO_BUSY_FATAL)
                .put(ErrorCode.INVALID_PARAMETER, ResponseCode.BAD_REQUEST_FATAL)
                .put(ErrorCode.UNKNOWN, ResponseCode.INTERNAL_ERROR_FATAL)
            .build());

}

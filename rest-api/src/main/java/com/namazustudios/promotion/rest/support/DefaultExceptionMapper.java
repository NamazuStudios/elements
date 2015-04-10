package com.namazustudios.promotion.rest.support;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.namazustudios.promotion.exception.BaseException;
import com.namazustudios.promotion.exception.ErrorCode;
import com.namazustudios.promotion.model.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by patricktwohig on 4/10/15.
 */
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    private static final Map<ErrorCode, Response.Status> HTTP_STATUS_MAP = Maps.immutableEnumMap(
            new ImmutableMap.Builder<ErrorCode, Response.Status>()
                        .put(ErrorCode.DUPLICATE, Response.Status.CONFLICT)
                        .put(ErrorCode.FORBIDDEN, Response.Status.FORBIDDEN)
                        .put(ErrorCode.INVALID_DATA, Response.Status.BAD_REQUEST)
                        .put(ErrorCode.NOT_FOUND, Response.Status.NOT_FOUND)
                        .put(ErrorCode.OVERLOAD, Response.Status.SERVICE_UNAVAILABLE)
                        .put(ErrorCode.INVALID_PARAMETER, Response.Status.BAD_REQUEST)
                        .put(ErrorCode.UNKNOWN, Response.Status.INTERNAL_SERVER_ERROR)
                    .build());

    public static final Response.Status getStatusForCode(Object code) {
        final Response.Status status = HTTP_STATUS_MAP.get(code);
        return status == null ? Response.Status.INTERNAL_SERVER_ERROR : status;
    }

    @Override
    public Response toResponse(Exception exception) {

        try {
            throw exception;
        } catch (BaseException ex) {

            final ErrorResponse errorResponse = new ErrorResponse();

            errorResponse.setMessage(ex.getMessage());
            errorResponse.setCode(ex.getCode().toString());

            return Response
                        .status(getStatusForCode(ex.getCode()))
                        .entity(errorResponse)
                    .build();

        } catch (Exception ex) {

            final ErrorResponse errorResponse = new ErrorResponse();

            errorResponse.setMessage(ex.getMessage());
            errorResponse.setCode(ErrorCode.UNKNOWN.toString());

            return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(errorResponse)
                    .build();

        }

    }

}

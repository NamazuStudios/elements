package com.namazustudios.socialengine.jrpc;

import com.namazustudios.socialengine.model.ErrorResponse;
import com.namazustudios.socialengine.rt.exception.BadParameterException;
import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.namazustudios.socialengine.rt.ResponseCode.INTERNAL_ERROR_FATAL;
import static com.namazustudios.socialengine.rt.jrpc.JsonRpcError.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class JsonRpcExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(final Exception exception) {

        final var jrpcError = new JsonRpcError();
        final var errorResponse = new ErrorResponse();

        jrpcError.setData(errorResponse);
        jrpcError.setMessage(exception.getMessage() == null
                ? exception.getClass().getName()
                : exception.getMessage()
        );

        errorResponse.setMessage(jrpcError.getMessage());

        if (exception instanceof MethodNotFoundException) {
            final var mnfe = (MethodNotFoundException) exception;
            jrpcError.setCode(METHOD_NOT_FOUND);
            errorResponse.setCode(mnfe.getResponseCode().toString());
        } else if (exception instanceof BadParameterException) {
            final var bpe = (BadParameterException) exception;
            jrpcError.setCode(INVALID_PARAMETERS);
            errorResponse.setCode(bpe.getResponseCode().toString());
        } else if (exception instanceof com.namazustudios.socialengine.exception.BaseException) {
            final var bex = (com.namazustudios.socialengine.exception.BaseException) exception;
            final var code = bex.getCode().ordinal();
            jrpcError.setCode(code);
            errorResponse.setCode(bex.getCode().toString());
        } else if (exception instanceof com.namazustudios.socialengine.rt.exception.BaseException) {
            final var bex = (com.namazustudios.socialengine.rt.exception.BaseException) exception;
            final var code = bex.getResponseCode().getCode();
            jrpcError.setCode(code);
            errorResponse.setCode(bex.getResponseCode().toString());
        } else {
            jrpcError.setCode(INTERNAL_ERROR);
            errorResponse.setCode(INTERNAL_ERROR_FATAL.toString());
        }

        return Response
                .status(BAD_REQUEST)
                .entity(jrpcError)
                .build();

    }

}

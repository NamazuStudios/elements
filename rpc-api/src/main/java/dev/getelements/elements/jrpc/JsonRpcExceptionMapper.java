package dev.getelements.elements.jrpc;

import dev.getelements.elements.exception.ErrorCode;
import dev.getelements.elements.model.ErrorResponse;
import dev.getelements.elements.rt.exception.BadParameterException;
import dev.getelements.elements.rt.exception.MethodNotFoundException;
import dev.getelements.elements.rt.jrpc.JsonRpcError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.Map;

import static dev.getelements.elements.rt.ResponseCode.INTERNAL_ERROR_FATAL;
import static dev.getelements.elements.rt.jrpc.JsonRpcError.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
public class JsonRpcExceptionMapper implements ExceptionMapper<Exception> {

    private static final Map<ErrorCode, Response.Status> HTTP_STATUS_MAP = Map.of(
            ErrorCode.DUPLICATE, Response.Status.CONFLICT,
            ErrorCode.UNAUTHORIZED, Response.Status.UNAUTHORIZED,
            ErrorCode.CONFLICT, Response.Status.CONFLICT,
            ErrorCode.FORBIDDEN, Response.Status.FORBIDDEN,
            ErrorCode.INVALID_DATA, Response.Status.BAD_REQUEST,
            ErrorCode.INVALID_PARAMETER, Response.Status.BAD_REQUEST,
            ErrorCode.NOT_FOUND, Response.Status.NOT_FOUND,
            ErrorCode.OVERLOAD, Response.Status.SERVICE_UNAVAILABLE,
            ErrorCode.UNKNOWN, INTERNAL_SERVER_ERROR,
            ErrorCode.NOT_IMPLEMENTED, Response.Status.NOT_IMPLEMENTED
    );

    @Override
    public Response toResponse(final Exception exception) {

        final var jrpcError = new JsonRpcError();
        final var errorResponse = new ErrorResponse();

        var status = INTERNAL_SERVER_ERROR;

        jrpcError.setData(errorResponse);
        jrpcError.setMessage(exception.getMessage() == null
                ? exception.getClass().getName()
                : exception.getMessage()
        );

        errorResponse.setMessage(jrpcError.getMessage());

        if (exception instanceof MethodNotFoundException) {
            final var mnfe = (MethodNotFoundException) exception;
            status = BAD_REQUEST;
            jrpcError.setCode(METHOD_NOT_FOUND);
            errorResponse.setCode(mnfe.getResponseCode().toString());
        } else if (exception instanceof BadParameterException) {
            final var bpe = (BadParameterException) exception;
            status = BAD_REQUEST;
            jrpcError.setCode(INVALID_PARAMETERS);
            errorResponse.setCode(bpe.getResponseCode().toString());
        } else if (exception instanceof dev.getelements.elements.exception.BaseException) {
            final var bex = (dev.getelements.elements.exception.BaseException) exception;
            final var code = bex.getCode().ordinal();
            jrpcError.setCode(code);
            errorResponse.setCode(bex.getCode().toString());
        } else if (exception instanceof dev.getelements.elements.rt.exception.BaseException) {
            final var bex = (dev.getelements.elements.rt.exception.BaseException) exception;
            final var code = bex.getResponseCode().getCode();
            jrpcError.setCode(code);
            errorResponse.setCode(bex.getResponseCode().toString());
        } else {
            jrpcError.setCode(INTERNAL_ERROR);
            errorResponse.setCode(INTERNAL_ERROR_FATAL.toString());
        }

        return Response
                .status(status)
                .entity(jrpcError)
                .build();

    }

}

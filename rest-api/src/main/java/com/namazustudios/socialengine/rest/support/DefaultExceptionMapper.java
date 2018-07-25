package com.namazustudios.socialengine.rest.support;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.ErrorCode;
import com.namazustudios.socialengine.exception.ValidationFailureException;
import com.namazustudios.socialengine.model.ErrorResponse;
import com.namazustudios.socialengine.model.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by patricktwohig on 4/10/15.
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    private static final Map<ErrorCode, Response.Status> HTTP_STATUS_MAP = Maps.immutableEnumMap(
        new ImmutableMap.Builder<ErrorCode, Response.Status>()
            .put(ErrorCode.DUPLICATE, Response.Status.CONFLICT)
            .put(ErrorCode.UNAUTHORIZED, Response.Status.UNAUTHORIZED)
            .put(ErrorCode.FORBIDDEN, Response.Status.FORBIDDEN)
            .put(ErrorCode.INVALID_DATA, Response.Status.BAD_REQUEST)
            .put(ErrorCode.INVALID_PARAMETER, Response.Status.BAD_REQUEST)
            .put(ErrorCode.NOT_FOUND, Response.Status.NOT_FOUND)
            .put(ErrorCode.OVERLOAD, Response.Status.SERVICE_UNAVAILABLE)
            .put(ErrorCode.UNKNOWN, Response.Status.INTERNAL_SERVER_ERROR)
            .put(ErrorCode.NOT_IMPLEMENTED, Response.Status.NOT_IMPLEMENTED)
        .build());

    public static final Response.Status getStatusForCode(final Object code) {
        final Response.Status status = HTTP_STATUS_MAP.get(code);
        return status == null ? Response.Status.INTERNAL_SERVER_ERROR : status;
    }

    @Override
    public Response toResponse(Exception exception) {

        try {
            throw exception;
        } catch (ValidationFailureException ex) {

            final ValidationErrorResponse errorResponse = new ValidationErrorResponse();
            final List<ConstraintViolation<Object>> violationList = ex.getConstraintViolations();

            LOG.info("Caught validation failure exception while processing request.", ex);

            errorResponse.setMessage(ex.getMessage());
            errorResponse.setCode(ex.getCode().toString());
            errorResponse.setValidationFailureMessages(violationList
                .stream()
                .map(v -> v.getPropertyPath() + " - " + v.getMessage())
                .collect(toList()));

            return Response
                    .status(getStatusForCode(ex.getCode()))
                    .entity(errorResponse)
                .build();

        } catch (BaseException ex) {

            final ErrorResponse errorResponse = new ErrorResponse();

            LOG.info("Caught expected exception while processing request.", ex);

            errorResponse.setMessage(ex.getMessage());
            errorResponse.setCode(ex.getCode().toString());

            return Response.status(getStatusForCode(ex.getCode()))
                           .entity(errorResponse)
                           .build();

        } catch (WebApplicationException wex) {

            final Response response = wex.getResponse();

            final ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage(wex.getMessage());
            errorResponse.setCode(ErrorCode.UNKNOWN.toString());

            return Response.fromResponse(response)
                           .entity(errorResponse)
                           .build();

        } catch (Exception ex) {

            final ErrorResponse errorResponse = new ErrorResponse();

            LOG.warn("Caught unknown exception while processing request.", ex);

            errorResponse.setMessage(ex.getMessage());
            errorResponse.setCode(ErrorCode.UNKNOWN.toString());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(errorResponse)
                           .build();

        }

    }

}

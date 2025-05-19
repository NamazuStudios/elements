package dev.getelements.elements.sdk.jakarta.rs;

import dev.getelements.elements.sdk.model.ErrorResponse;
import dev.getelements.elements.sdk.model.ValidationErrorResponse;
import dev.getelements.elements.sdk.model.exception.*;
import dev.getelements.elements.sdk.model.health.HealthErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;

import static dev.getelements.elements.sdk.model.Headers.BEARER;
import static java.util.stream.Collectors.toList;
import static jakarta.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE;

/**
 * Created by patricktwohig on 4/10/15.
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    public static final Map<ErrorCode, Response.Status> HTTP_STATUS_MAP = Map.of(
        ErrorCode.DUPLICATE, Response.Status.CONFLICT,
        ErrorCode.UNAUTHORIZED, Response.Status.UNAUTHORIZED,
        ErrorCode.CONFLICT, Response.Status.CONFLICT,
        ErrorCode.FORBIDDEN, Response.Status.FORBIDDEN,
        ErrorCode.INVALID_DATA, Response.Status.BAD_REQUEST,
        ErrorCode.INVALID_PARAMETER, Response.Status.BAD_REQUEST,
        ErrorCode.NOT_FOUND, Response.Status.NOT_FOUND,
        ErrorCode.OVERLOAD, Response.Status.SERVICE_UNAVAILABLE,
        ErrorCode.UNKNOWN, Response.Status.INTERNAL_SERVER_ERROR,
        ErrorCode.NOT_IMPLEMENTED, Response.Status.NOT_IMPLEMENTED
    );

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

            return Response.status(getStatusForCode(ex.getCode()))
                .entity(errorResponse)
                .build();

        } catch (UnhealthyException ex) {

            LOG.info("Caught validation failure exception while processing request.", ex);

            final var errorResponse = new HealthErrorResponse();
            errorResponse.setHealthStatus(ex.getHealthStatus());
            errorResponse.setCode(ex.getCode().toString());
            errorResponse.setMessage(ex.getMessage());

            return Response.status(getStatusForCode(ex.getCode()))
                .entity(errorResponse)
                .build();

        } catch (UnauthorizedException ex) {

            final ErrorResponse errorResponse = new ErrorResponse();

            LOG.info("Caught validation failure exception while processing request.", ex);

            errorResponse.setMessage(ex.getMessage());
            errorResponse.setCode(ex.getCode().toString());

            return Response.status(getStatusForCode(ex.getCode()))
                    .entity(errorResponse)
                    .header(WWW_AUTHENTICATE, BEARER)
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

package dev.getelements.elements.sdk.jakarta.rs;

import dev.getelements.elements.sdk.model.ErrorResponse;
import dev.getelements.elements.sdk.model.ValidationErrorResponse;
import dev.getelements.elements.sdk.model.auth.MfaChallengeErrorResponse;
import dev.getelements.elements.sdk.model.exception.*;
import dev.getelements.elements.sdk.model.exception.auth.MfaChallengeRequiredException;
import dev.getelements.elements.sdk.model.exception.auth.OAuth2Exception;
import dev.getelements.elements.sdk.model.health.HealthErrorResponse;
import dev.getelements.elements.sdk.model.oidc.OAuth2ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
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

    public static final Map<ErrorCode, Response.Status> HTTP_STATUS_MAP = Map.ofEntries(
        Map.entry(ErrorCode.DUPLICATE, Response.Status.CONFLICT),
        Map.entry(ErrorCode.UNAUTHORIZED, Response.Status.UNAUTHORIZED),
        Map.entry(ErrorCode.CONFLICT, Response.Status.CONFLICT),
        Map.entry(ErrorCode.IDENTITY_ALREADY_LINKED, Response.Status.CONFLICT),
        Map.entry(ErrorCode.FORBIDDEN, Response.Status.FORBIDDEN),
        Map.entry(ErrorCode.INVALID_DATA, Response.Status.BAD_REQUEST),
        Map.entry(ErrorCode.INVALID_PARAMETER, Response.Status.BAD_REQUEST),
        Map.entry(ErrorCode.NOT_FOUND, Response.Status.NOT_FOUND),
        Map.entry(ErrorCode.OVERLOAD, Response.Status.SERVICE_UNAVAILABLE),
        Map.entry(ErrorCode.RATE_LIMITED, Response.Status.TOO_MANY_REQUESTS),
        Map.entry(ErrorCode.UNKNOWN, Response.Status.INTERNAL_SERVER_ERROR),
        Map.entry(ErrorCode.NOT_IMPLEMENTED, Response.Status.NOT_IMPLEMENTED),
        Map.entry(ErrorCode.MFA_REQUIRED, Response.Status.UNAUTHORIZED)
    );

    public static final Response.Status getStatusForCode(final Object code) {
        final Response.Status status = HTTP_STATUS_MAP.get(code);
        return status == null ? Response.Status.INTERNAL_SERVER_ERROR : status;
    }

    /**
     * Maps an RFC 6749 OAuth 2.0 error code to its HTTP status, per section 5.2 and related specifications.
     *
     * @param error the OAuth 2.0 error code
     * @return the HTTP status to return
     */
    public static final Response.Status getStatusForOAuth2Error(final String error) {
        return switch (error == null ? "" : error) {
            case "invalid_client" -> Response.Status.UNAUTHORIZED;
            case "server_error" -> Response.Status.INTERNAL_SERVER_ERROR;
            case "temporarily_unavailable" -> Response.Status.SERVICE_UNAVAILABLE;
            default -> Response.Status.BAD_REQUEST;
        };
    }

    private static String fieldName(final String propertyPath) {
        final int dot = propertyPath.lastIndexOf('.');
        return dot >= 0 ? propertyPath.substring(dot + 1) : propertyPath;
    }

    @Override
    public Response toResponse(Exception exception) {

        try {
            throw exception;
        } catch (ValidationFailureException ex) {

            final ValidationErrorResponse errorResponse = new ValidationErrorResponse();
            final List<ConstraintViolation<Object>> violationList = ex.getConstraintViolations();

            LOG.debug("Caught validation failure exception while processing request.", ex);

            final List<String> messages = violationList
                .stream()
                .map(v -> fieldName(v.getPropertyPath().toString()) + ": " + v.getMessage())
                .collect(toList());

            errorResponse.setMessage("Validation failed: " + String.join("; ", messages));
            errorResponse.setCode(ex.getCode().toString());
            errorResponse.setValidationFailureMessages(messages);

            return Response.status(getStatusForCode(ex.getCode()))
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();

        } catch (ConstraintViolationException ex) {

            final ValidationErrorResponse errorResponse = new ValidationErrorResponse();

            LOG.debug("Caught constraint violation exception while processing request.", ex);

            final List<String> messages = ex.getConstraintViolations()
                .stream()
                .map(v -> fieldName(v.getPropertyPath().toString()) + ": " + v.getMessage())
                .collect(toList());

            errorResponse.setMessage("Validation failed: " + String.join("; ", messages));
            errorResponse.setCode(ErrorCode.INVALID_DATA.toString());
            errorResponse.setValidationFailureMessages(messages);

            return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();

        } catch (UnhealthyException ex) {

            LOG.debug("Caught validation failure exception while processing request.", ex);

            final var errorResponse = new HealthErrorResponse();
            errorResponse.setHealthStatus(ex.getHealthStatus());
            errorResponse.setCode(ex.getCode().toString());
            errorResponse.setMessage(ex.getMessage());

            return Response.status(getStatusForCode(ex.getCode()))
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();

        } catch (MfaChallengeRequiredException ex) {

            LOG.info("Caught MFA challenge required exception while processing request.", ex);

            final var errorResponse = new MfaChallengeErrorResponse();
            errorResponse.setChallengeId(ex.getChallengeId());
            errorResponse.setExpiresAt(ex.getExpiresAt());
            errorResponse.setCode(ex.getCode().toString());
            errorResponse.setMessage(ex.getMessage());

            return Response.status(getStatusForCode(ex.getCode()))
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        } catch (UnauthorizedException ex) {

            final ErrorResponse errorResponse = new ErrorResponse();

            LOG.debug("Caught validation failure exception while processing request.", ex);

            errorResponse.setMessage(ex.getMessage());
            errorResponse.setCode(ex.getCode().toString());

            return Response.status(getStatusForCode(ex.getCode()))
                    .entity(errorResponse)
                    .header(WWW_AUTHENTICATE, BEARER)
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        } catch (OAuth2Exception ex) {

            LOG.debug("Caught OAuth 2.0 provider exception while processing request.", ex);

            final var errorResponse = new OAuth2ErrorResponse();
            errorResponse.setError(ex.getError());
            errorResponse.setErrorDescription(ex.getErrorDescription());

            final var response = Response.status(getStatusForOAuth2Error(ex.getError()))
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON);

            if ("invalid_client".equals(ex.getError())) {
                response.header(WWW_AUTHENTICATE, "Basic realm=\"elements\"");
            }

            return response.build();

        } catch (BaseException ex) {

            final ErrorResponse errorResponse = new ErrorResponse();
            LOG.debug("Caught expected exception while processing request.", ex);

            errorResponse.setMessage(ex.getMessage());
            errorResponse.setCode(ex.getCode().toString());

            return Response.status(getStatusForCode(ex.getCode()))
                           .entity(errorResponse)
                           .type(MediaType.APPLICATION_JSON)
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

            // Unwrap Guice ProvisionException without adding a compile-time Guice dependency.
            // When providers throw inside an element scope, Guice wraps the real exception in
            // ProvisionException; unwrapping it lets the normal handler return the right status.
            if (ex.getCause() instanceof Exception
                    && ex.getClass().getName().endsWith("ProvisionException")) {
                return toResponse((Exception) ex.getCause());
            }

            // Walk the cause chain: if a known BaseException is wrapped (e.g. by a Guice
            // ProvisionException), re-dispatch it so the proper status code is returned.
            for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof BaseException) {
                    return toResponse((Exception) cause);
                }
            }

            final ErrorResponse errorResponse = new ErrorResponse();

            LOG.warn("Caught unknown exception while processing request.", ex);

            errorResponse.setMessage(ex.getMessage());
            errorResponse.setCode(ErrorCode.UNKNOWN.toString());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(errorResponse)
                           .type(MediaType.APPLICATION_JSON)
                           .build();

        }

    }

}

package dev.getelements.elements.exception;

import dev.getelements.elements.sdk.model.exception.BaseException;
import dev.getelements.elements.sdk.model.exception.ErrorCode;

import java.util.Map;

import static dev.getelements.elements.sdk.model.exception.ErrorCode.*;
import static jakarta.servlet.http.HttpServletResponse.*;

/**
 * Created by patricktwohig on 8/3/17.
 */
public class StatusMapping {

    public static final Map<ErrorCode, Integer> HTTP_STATUS_MAP = Map.ofEntries(
            Map.entry(DUPLICATE, SC_CONFLICT),
            Map.entry(FORBIDDEN, SC_FORBIDDEN),
            Map.entry(UNAUTHORIZED, SC_UNAUTHORIZED),
            Map.entry(INVALID_DATA, SC_BAD_REQUEST),
            Map.entry(INVALID_PARAMETER, SC_BAD_REQUEST),
            Map.entry(NOT_FOUND, SC_NOT_FOUND),
            Map.entry(OVERLOAD, SC_SERVICE_UNAVAILABLE),
            Map.entry(UNKNOWN, SC_INTERNAL_SERVER_ERROR),
            Map.entry(NOT_IMPLEMENTED, SC_NOT_IMPLEMENTED)
        );

    public static int map(final BaseException ex) {
        return HTTP_STATUS_MAP.getOrDefault(ex.getCode(), SC_INTERNAL_SERVER_ERROR);
    }

}

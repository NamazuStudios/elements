package com.namazustudios.socialengine.exception;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.namazustudios.socialengine.exception.ErrorCode.*;
import static javax.servlet.http.HttpServletResponse.*;

/**
 * Created by patricktwohig on 8/3/17.
 */
public class StatusMapping {

    private static final Map<ErrorCode, Integer> HTTP_STATUS_MAP = Maps.immutableEnumMap(
            new ImmutableMap.Builder<ErrorCode, Integer>()
                    .put(DUPLICATE, SC_CONFLICT)
                    .put(FORBIDDEN, SC_FORBIDDEN)
                    .put(UNAUTHORIZED, SC_UNAUTHORIZED)
                    .put(INVALID_DATA, SC_BAD_REQUEST)
                    .put(INVALID_PARAMETER, SC_BAD_REQUEST)
                    .put(NOT_FOUND, SC_NOT_FOUND)
                    .put(OVERLOAD, SC_SERVICE_UNAVAILABLE)
                    .put(UNKNOWN, SC_INTERNAL_SERVER_ERROR)
                    .put(NOT_IMPLEMENTED, SC_NOT_IMPLEMENTED)
                .build());

    public static int map(final BaseException ex) {
        return HTTP_STATUS_MAP.getOrDefault(ex.getCode(), SC_INTERNAL_SERVER_ERROR);
    }

}

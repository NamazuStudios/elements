package com.namazustudios.socialengine.appserve;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import com.namazustudios.socialengine.Headers;
import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.ErrorCode;
import com.namazustudios.socialengine.model.ErrorResponse;
import com.namazustudios.socialengine.rt.ExceptionMapper;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.SimpleResponse;
import com.namazustudios.socialengine.rt.http.HttpStatus;

import java.util.Map;
import java.util.function.Consumer;

public class ServiceExceptionMapper implements ExceptionMapper<BaseException> {

    private static final Map<ErrorCode, HttpStatus> CODE_MAP = ImmutableMap.<ErrorCode, HttpStatus>builder()
            .put(ErrorCode.DUPLICATE, HttpStatus.CONFLICT)
            .put(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED)
            .put(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN)
            .put(ErrorCode.INVALID_DATA, HttpStatus.BAD_REQUEST)
            .put(ErrorCode.INVALID_PARAMETER, HttpStatus.BAD_REQUEST)
            .put(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND)
            .put(ErrorCode.OVERLOAD, HttpStatus.SERVICE_UNAVAILABLE)
            .put(ErrorCode.UNKNOWN, HttpStatus.INTERNAL_SERVER_ERROR)
            .put(ErrorCode.NOT_IMPLEMENTED, HttpStatus.NOT_IMPLEMENTED)
        .build();

    @Override
    public void map(final BaseException exception, final Consumer<Response> responseConsumer) {

        // THe system needs some retooling for exceptions because these codes end up getting reversed mapped downstream
        // but until we can unify the two bits of the API this needs to be re-worked. For not this is a bit convoluted
        // but it should work with the rest of the system. Each Elements specific (non-RT) error code is mapped
        // and then handed downstream as a custom error response.

        final ErrorCode code = exception.getCode();
        final ErrorResponse errorResponse = new ErrorResponse();
        final SimpleResponse.Builder builder = new SimpleResponse.Builder();
        final HttpStatus httpStatus = CODE_MAP.getOrDefault(code, HttpStatus.INTERNAL_SERVER_ERROR);

        errorResponse.setCode(httpStatus.toString());
        errorResponse.setMessage(exception.getMessage());

        builder.codeRaw(httpStatus.getCode())
               .payload(errorResponse);

        if (ErrorCode.UNAUTHORIZED.equals(code)) {
            builder.header(HttpHeaders.WWW_AUTHENTICATE, Headers.BEARER);
        }

        responseConsumer.accept(builder.build());

    }

}

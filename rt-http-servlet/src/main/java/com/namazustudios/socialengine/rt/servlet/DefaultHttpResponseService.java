package com.namazustudios.socialengine.rt.servlet;

import com.google.common.collect.ImmutableMap;
import com.namazustudios.socialengine.rt.NamedHeaders;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.ResponseCode;
import com.namazustudios.socialengine.rt.http.HttpResponse;
import com.namazustudios.socialengine.rt.http.HttpStatus;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class DefaultHttpResponseService implements HttpResponseService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHttpResponseService.class);

    private static final Map<ResponseCode, HttpStatus> HTTP_STATUS_MAP =
        new ImmutableMap.Builder<ResponseCode, HttpStatus>()
            .put(ResponseCode.OK, HttpStatus.OK)
            .put(ResponseCode.BAD_REQUEST_FATAL, HttpStatus.BAD_REQUEST)
            .put(ResponseCode.BAD_REQUEST_RETRY, HttpStatus.BAD_REQUEST)
            .put(ResponseCode.BAD_REQUEST_INVALID_CONTENT, HttpStatus.BAD_REQUEST)
            .put(ResponseCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT)
            .put(ResponseCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND)
            .put(ResponseCode.VERB_NOT_SUPPORTED, HttpStatus.METHOD_NOT_ALLOWED)
            .put(ResponseCode.OPERATION_NOT_FOUND, HttpStatus.NOT_FOUND)
            .put(ResponseCode.UNACCEPTABLE_CONTENT, HttpStatus.NOT_ACCEPTABLE)
            .put(ResponseCode.UNSUPPORTED_MEDIA_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .put(ResponseCode.NOT_FOUND, HttpStatus.NOT_FOUND)
            .put(ResponseCode.FAILED_AUTH_RETRY, HttpStatus.UNAUTHORIZED)
            .put(ResponseCode.FAILED_AUTH_FATAL, HttpStatus.FORBIDDEN)
            .put(ResponseCode.TOO_BUSY_FATAL, HttpStatus.SERVICE_UNAVAILABLE)
        .build();

    private Map<String , PayloadWriter> writersByContentType;

    @Override
    public void write(final HttpResponse toWrite,
                      final HttpServletResponse destination) {
        try {
            doWrite(toWrite, destination);
        } catch (Exception ex) {
            logAndSendInternalServerError(destination, ex);
        }
    }

    private void doWrite(final HttpResponse toWrite,
                         final HttpServletResponse destination) throws ServletException, IOException {

        final HttpContent responseContent = toWrite.getManifestMetadata().getPreferredResponseContent();
        final PayloadWriter payloadWriter = getWritersByContentType().get(responseContent.getType());

        if (payloadWriter == null) {
            logger.error("No writer specified for content {}", responseContent);
            destination.sendError(SC_INTERNAL_SERVER_ERROR);
        } else {
            doWrite(toWrite, destination, responseContent, payloadWriter);
        }

    }

    private void doWrite(final HttpResponse toWrite,
                         final HttpServletResponse destination,
                         final HttpContent responseContent,
                         final PayloadWriter payloadWriter) throws ServletException, IOException {

        final Object payload = toWrite.getPayload(responseContent.getPayloadType());

        setStaticHeaders(responseContent, destination);
        setResponseHeaders(toWrite, destination);

        if (destination.getHeaders(CONTENT_TYPE).isEmpty()) {
            // We only set this if nobody had previously set the Content-Type header.  The response itself can, if it
            // so desires, modify the Conten-Type (even if it doesn't make any sense).  In most cases the client code
            // will not want to set this, so we should set this here in the container.
            destination.setHeader(CONTENT_TYPE, responseContent.getType());
        }

        setStatusCode(toWrite, destination, payload);

        if (payload != null) {
            payloadWriter.write(payload, destination.getOutputStream());
        }

    }

    private void setStaticHeaders(final HttpContent responseContent, final HttpServletResponse destination) {
        if (responseContent.getStaticHeaders() != null) {
            for (final Map.Entry<String, String> entry : responseContent.getStaticHeaders().entrySet()) {
                destination.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setResponseHeaders(final HttpResponse toWrite, final HttpServletResponse destination) {

        final NamedHeaders namedHeaders = toWrite.getResponseHeader();

        for (final String header : namedHeaders.getHeaderNames()) {
            namedHeaders.getHeaders(header).get().forEach(o -> destination.addHeader(header, o.toString()));
        }

    }

    private void logAndSendInternalServerError(final HttpServletResponse destination, final Exception ex) {

        logger.error("Caught exception formulating response.", ex);

        try {
            destination.sendError(SC_INTERNAL_SERVER_ERROR);
        } catch (IOException $ex) {
            logger.error("Caught exception sending response.", $ex);
        }

    }

    private void setStatusCode(final HttpResponse toWrite,
                               final HttpServletResponse destination,
                               final Object payload) throws IOException {

        final int code = toWrite.getResponseHeader().getCode();

        // For reserved codes, we provide our own mapping because we're using RT error codes and relying on the mapping
        // provided therein.

        if (ResponseCode.isReserved(code)) {
            // Some more complicated logic goes into writing reserved codes.  Reserved codes are processed by the
            // system and are handed through.
            writeReservedCode(code, payload, destination);
        } else {
            destination.setStatus(code);
        }

    }

    private void writeReservedCode(final int code,
                                   final Object payload,
                                   final HttpServletResponse destination) throws IOException {
        if (ResponseCode.OK.getCode() == code) {
            if (payload == null) {
                destination.setStatus(HttpStatus.NO_CONTENT.getCode());
            } else {
                destination.setStatus(HttpStatus.OK.getCode());
            }
        } else {
            final ResponseCode eCode = ResponseCode.getCodeForValue(code);
            final HttpStatus httpStatus = HTTP_STATUS_MAP.getOrDefault(eCode, HttpStatus.INTERNAL_SERVER_ERROR);
            destination.setStatus(httpStatus.getCode());
        }
    }

    public Map<String, PayloadWriter> getWritersByContentType() {
        return writersByContentType;
    }

    @Inject
    public void setWritersByContentType(final Map<String, PayloadWriter> writersByContentType) {
        this.writersByContentType = writersByContentType;
    }

}

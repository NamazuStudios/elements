package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.NamedHeaders;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.http.HttpResponse;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class DefaultResponseService implements HttpResponseService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResponseService.class);

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
            logger.info("No writer specified for content {}", responseContent);
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

        if (payload == null) {
            destination.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            destination.setStatus(HttpServletResponse.SC_OK);
            payloadWriter.write(payload, destination.getOutputStream());
        }

    }

    private void setStaticHeaders(final HttpContent responseContent, final HttpServletResponse destination) {
        for (final Map.Entry<String, String> entry : responseContent.getStaticHeaders().entrySet()) {
            destination.addHeader(entry.getKey(), entry.getValue());
        }
    }

    private void setResponseHeaders(final HttpResponse toWrite, final HttpServletResponse destination) {

        final NamedHeaders namedHeaders = toWrite.getResponseHeader();

        for (final String header : namedHeaders.getHeaderNames()) {
            namedHeaders.getHeaders(header).forEach(o -> destination.addHeader(header, o.toString()));
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

    public Map<String, PayloadWriter> getWritersByContentType() {
        return writersByContentType;
    }

    @Inject
    public void setWritersByContentType(Map<String, PayloadWriter> writersByContentType) {
        this.writersByContentType = writersByContentType;
    }

}

package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.NamedHeaders;
import com.namazustudios.socialengine.rt.http.HttpResponse;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class DefaultResponseService implements HttpResponseService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResponseService.class);

    private Map<String , EntityBodyWriter> writersByContentType;

    @Override
    public void write(final HttpResponse toWrite,
                      final HttpServletResponse destination) {
        try {
            doWrite(toWrite, destination);
        } catch (Exception ex) {
            logAndSendInternalServerError(destination, ex);
        }
    }

    private void doWrite(final HttpResponse toWrite, final HttpServletResponse destination) throws ServletException, IOException {

        final HttpContent responseContent = toWrite.getManifestMetadata().getPreferredResponseContent();
        final EntityBodyWriter entityBodyWriter = getWritersByContentType().get(responseContent.getType());

        if (entityBodyWriter == null) {
            logger.info("Not write specified for content {}", responseContent);
            return;
        }

        final Object payload = toWrite.getPayload(responseContent.getPayloadType());

        setStaticHeaders(responseContent, destination);
        setResponseHeaders(toWrite, destination);

        if (payload == null) {
            destination.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            destination.setStatus(HttpServletResponse.SC_OK);
            entityBodyWriter.writeEntityBody(payload, destination);
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
            destination.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IOException $ex) {
            logger.error("Caught exception sending response.", $ex);
        }

    }

    public Map<String, EntityBodyWriter> getWritersByContentType() {
        return writersByContentType;
    }

    @Inject
    public void setWritersByContentType(Map<String, EntityBodyWriter> writersByContentType) {
        this.writersByContentType = writersByContentType;
    }

}

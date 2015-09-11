package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.common.collect.ImmutableMap;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.EdgeRequestPathHandler;
import com.namazustudios.socialengine.rt.edge.EdgeResource;
import de.undercouch.bson4jackson.BsonFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Decodes data into into BSON objects which can then be handed to the rest of the
 * filter chain.
 *
 * Created by patricktwohig on 7/26/15.
 */
public class ServerBSONProtocolDecoder extends CumulativeProtocolDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(ServerBSONProtocolDecoder.class);

    public static final int BSON_DOCUMENT_LENGTH_LENGTH = 4;

    private static final AttributeKey BUFFER_STATE = new AttributeKey(ServerBSONProtocolDecoder.class, "BufferState");

    private static final AttributeKey DOCUMENT_SIZE = new AttributeKey(ServerBSONProtocolDecoder.class, "DocumentSize");

    @Inject
    @Named(Constants.BSON_OBJECT_MAPPER)
    private ObjectMapper objectMapper;

    @Inject
    private ResourceService<EdgeResource> edgeResourceService;

    @Override
    protected boolean doDecode(final IoSession session,
                               final IoBuffer in,
                               final ProtocolDecoderOutput out) throws Exception {

        final BufferState bufferState = getBufferState(session);

        switch (bufferState) {

            case DOCUMENT_LENGTH:
                checkAndReadDocumentLength(session, in);
                return false;

            case DOCUMENT:
                return checkAndReadDocument(session, in, out);

            default:
                throw new IllegalStateException("Invalid state " + bufferState);

        }

    }

    private void checkAndReadDocumentLength(final IoSession ioSession, final IoBuffer in) {

        if (in.remaining() >= BSON_DOCUMENT_LENGTH_LENGTH) {
            final int documentLength = in.getInt(in.position());
            setDocumentLength(ioSession, documentLength);
            setBufferState(ioSession, BufferState.DOCUMENT);
        }

    }

    private boolean checkAndReadDocument(final IoSession ioSession,
                                         final IoBuffer in,
                                         final ProtocolDecoderOutput out) throws IOException {

        final int documentLength = getDocumentLength(ioSession);

        if (in.remaining() >= (BSON_DOCUMENT_LENGTH_LENGTH + documentLength)) {

            final byte[] buffer = new byte[BSON_DOCUMENT_LENGTH_LENGTH + documentLength];
            in.get(buffer);

            try {

                final SimpleRequest simpleRequest = objectMapper.readValue(buffer, SimpleRequest.class);
                final Path path = new Path(simpleRequest.getHeader().getPath());

                final EdgeRequestPathHandler edgeRequestPathHandler = edgeResourceService
                        .getResource(path)
                        .getHandler(simpleRequest.getHeader().getMethod());

                final Class<?> payloadType = edgeRequestPathHandler.getPayloadType();

                final Object payload = objectMapper.convertValue(simpleRequest.getPayload(), payloadType);
                simpleRequest.setPayload(payload);

                out.write(simpleRequest);

            } catch (Exception ex) {
                closeWithBadRequest(ex, ioSession);
                LOG.error("Received bad request.  Closing session {}.", ioSession, ex);
            } finally {
                ioSession.removeAttribute(BUFFER_STATE);
                ioSession.removeAttribute(DOCUMENT_SIZE);
            }

            return true;
        } else {
            return false;
        }

    }

    private BufferState getBufferState(final IoSession ioSession) {
        return (BufferState) ioSession.getAttribute(BUFFER_STATE, BufferState.DOCUMENT_LENGTH);
    }

    private void setBufferState(final IoSession ioSession, final BufferState bufferState) {
        ioSession.setAttribute(BUFFER_STATE, bufferState);
    }

    private Integer getDocumentLength(final IoSession ioSession) {
        return (Integer) ioSession.getAttribute(DOCUMENT_SIZE, 0);
    }

    private void setDocumentLength(final IoSession ioSession, int documentSize) {
        ioSession.setAttribute(DOCUMENT_SIZE, documentSize);
    }

    private void closeWithBadRequest(final Exception ex, final IoSession ioSession) {

        final SimpleResponse simpleResponse = SimpleResponse.builder()
                .code(ResponseCode.BAD_REQUEST_FATAL)
                .payload(ex.getMessage())
            .build();

        ioSession.write(simpleResponse);
        ioSession.close(false);

    }

    private enum BufferState {

        /**
         * Indicates that the decoder is reading the document length.
         */
        DOCUMENT_LENGTH,

        /**
         * Indicates that hte decoder is reading the document itself.
         */
        DOCUMENT

    }

}

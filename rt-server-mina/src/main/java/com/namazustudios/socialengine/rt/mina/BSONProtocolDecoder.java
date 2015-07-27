package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.namazustudios.socialengine.rt.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Decodes data into into BSON objects which can then be handed to the rest of the
 * filter chain.
 *
 * Created by patricktwohig on 7/26/15.
 */
public class BSONProtocolDecoder extends CumulativeProtocolDecoder {

    public static final int BSON_DOCUMENT_LENGTH = 4;

    private static final AttributeKey BUFFER_STATE = new AttributeKey(BSONProtocolDecoder.class, "BufferState");

    private static final AttributeKey DOCUMENT_SIZE = new AttributeKey(BSONProtocolDecoder.class, "DocumentSize");

    private static final AttributeKey HEADER_DOCUMENT = new AttributeKey(BSONProtocolDecoder.class, "HeaderDocument");

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private PathHandlerService pathHandlerService;

    @Override
    protected boolean doDecode(final IoSession session,
                               final IoBuffer in,
                               final ProtocolDecoderOutput out) throws Exception {

        final BufferState bufferState = getBufferState(session);

        switch (bufferState) {

            case HEADER_LENGTH_READ:
                checkAndReadHeaderLength(session, in);
                return false;

            case HEADER_DOCUMENT_READ:
                checkAndReadHeaderDocument(session, in);
                return false;

            case PAYLOAD_LENGTH_READ:
                checkAndReadPayloadLength(session, in);
                return false;

            case PAYLOAD_DOCUMENT_READ:
                return checkAndReadPayloadDocument(session, in, out);

            default:
                throw new IllegalStateException("Invalid state " + bufferState);

        }

    }

    private void checkAndReadHeaderLength(final IoSession ioSession, final IoBuffer in) {

        if (in.remaining() >= BSON_DOCUMENT_LENGTH) {
            final int documentSize = in.getInt(in.position());
            setDocumentSize(ioSession, documentSize);
            setBufferState(ioSession, BufferState.HEADER_DOCUMENT_READ);
        }

    }

    private void checkAndReadHeaderDocument(final IoSession ioSession, final IoBuffer in) throws IOException {

        final int documentSize = getDocumentSize(ioSession);

        if (in.remaining() >= (BSON_DOCUMENT_LENGTH + documentSize)) {

            try (final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(in.buf())){
                final RequestHeader requestHeader = objectMapper.readValue(inputStream, SimpleRequestHeader.class);
                ioSession.setAttribute(HEADER_DOCUMENT, requestHeader);
                setBufferState(ioSession, BufferState.PAYLOAD_LENGTH_READ);
            }

        }

    }

    private void checkAndReadPayloadLength(final IoSession ioSession, final IoBuffer in) {

        if (in.remaining() >= BSON_DOCUMENT_LENGTH) {
            final int documentSize = in.getInt(in.position());
            setDocumentSize(ioSession, documentSize);
            setBufferState(ioSession, BufferState.PAYLOAD_DOCUMENT_READ);
        }

    }

    private boolean checkAndReadPayloadDocument(final IoSession ioSession,
                                                final IoBuffer in,
                                                final ProtocolDecoderOutput out) throws IOException {

        final int documentSize = getDocumentSize(ioSession);

        if (in.remaining() >= (BSON_DOCUMENT_LENGTH + documentSize)) {

            final RequestHeader requestHeader = getRequestHeader(ioSession);
            final PathHandler<?> pathHandler = pathHandlerService.getPathHandler(requestHeader);
            final Class<?> payloadType = pathHandler.getPayloadType();
            final SimpleRequest<Object> request = new SimpleRequest();

            try (final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(in.buf())) {

                final Object payload = objectMapper.readValue(inputStream, payloadType);

                request.setHeader(requestHeader);
                request.setPayload(payload);

            }

            //Remove all attributes from the session
            ioSession.removeAttribute(BUFFER_STATE);
            ioSession.removeAttribute(DOCUMENT_SIZE);
            ioSession.removeAttribute(HEADER_DOCUMENT);

            // Write the message to be handed downstream to the filters as
            // they see fit.
            out.write(request);

            return true;

        } else {
            return false;
        }

    }

    public BufferState getBufferState(final IoSession ioSession) {
        return (BufferState) ioSession.getAttribute(BUFFER_STATE, BufferState.HEADER_LENGTH_READ);
    }

    public void setBufferState(final IoSession ioSession, final BufferState bufferState) {
        ioSession.setAttribute(BUFFER_STATE, bufferState);
    }

    public Integer getDocumentSize(final IoSession ioSession) {
        return (Integer) ioSession.getAttribute(DOCUMENT_SIZE, 0);
    }

    public void setDocumentSize(final IoSession ioSession, int documentSize) {
        ioSession.setAttribute(DOCUMENT_SIZE, documentSize);
    }

    public RequestHeader getRequestHeader(final IoSession ioSession) {
        return (RequestHeader) ioSession.getAttribute(HEADER_DOCUMENT);
    }

    enum BufferState {

        HEADER_LENGTH_READ,
        HEADER_DOCUMENT_READ,

        PAYLOAD_LENGTH_READ,
        PAYLOAD_DOCUMENT_READ,

    }

}

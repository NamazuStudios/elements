package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.EdgeResource;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/**
 * Created by patricktwohig on 9/11/15.
 */
public class ClientBSONProtocolDecoder extends CumulativeProtocolDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(ClientBSONProtocolDecoder.class);

    public static final int BSON_MESSAGE_TYPE_LENGTH = 1;

    public static final int BSON_DOCUMENT_LENGTH_LENGTH = 4;

    private static final AttributeKey BUFFER_STATE = new AttributeKey(ClientBSONProtocolDecoder.class, "BufferState");

    private static final AttributeKey DOCUMENT_SIZE = new AttributeKey(ClientBSONProtocolDecoder.class, "DocumentSize");

    private static final AttributeKey MESSAGE_TYPE = new AttributeKey(ClientBSONProtocolDecoder.class, "MessageType");

    @Inject
    @Named(Constants.BSON_OBJECT_MAPPER)
    private ObjectMapper objectMapper;

    @Override
    protected boolean doDecode(final IoSession session,
                               final IoBuffer in,
                               final ProtocolDecoderOutput out) throws Exception {

        final BufferState bufferState = getBufferState(session);

        switch (bufferState) {

            case DOCUMENT_LENGTH:
                checkAndReadDocumentLengthAndType(session, in);
                return false;

            case DOCUMENT:
                return checkAndReadDocument(session, in, out);

            default:
                throw new IllegalStateException("Invalid state " + bufferState);

        }

    }

    private void checkAndReadDocumentLengthAndType(final IoSession ioSession, final IoBuffer in) {
        if (in.remaining() >= (BSON_DOCUMENT_LENGTH_LENGTH + BSON_MESSAGE_TYPE_LENGTH)) {
            final byte messageType = in.get();
            final int documentLength = in.getInt(in.position());
            setMessageType(ioSession, messageType);
            setDocumentLength(ioSession, documentLength);
            setBufferState(ioSession, BufferState.DOCUMENT);
        }
    }

    private boolean checkAndReadDocument(final IoSession ioSession,
                                         final IoBuffer in,
                                         final ProtocolDecoderOutput out) throws IOException {

        final int documentLength = getDocumentLength(ioSession);
        if (in.remaining() >= (BSON_DOCUMENT_LENGTH_LENGTH + documentLength)) {

            try {

                switch (BSONMessageType.get(getMessageType(ioSession))) {
                    case RESPONSE:
                        decodeResponse(ioSession, in, out);
                        break;
                    case EVENT:
                        decodeEvent(ioSession, in, out);
                        break;
                    default:
                        throw new IllegalStateException("Illegal message type " + getMessageType(ioSession));
                }

            } catch (Exception ex) {
                closeWithBadMessage(ex, ioSession);
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

    private void decodeEvent(final IoSession ioSession,
                             final IoBuffer in,
                             final ProtocolDecoderOutput out) throws Exception {

        final int documentLength = getDocumentLength(ioSession);
        final byte[] buffer = new byte[BSON_DOCUMENT_LENGTH_LENGTH + documentLength];
        in.get(buffer);

        final SimpleEvent simpleEvent = objectMapper.readValue(buffer, SimpleEvent.class);
        out.write(simpleEvent);

    }

    private void decodeResponse(final IoSession ioSession,
                                final IoBuffer in,
                                final ProtocolDecoderOutput out) throws Exception {

        final int documentLength = getDocumentLength(ioSession);
        final byte[] buffer = new byte[BSON_DOCUMENT_LENGTH_LENGTH + documentLength];
        in.get(buffer);

        final SimpleResponse simpleResponse = objectMapper.readValue(buffer, SimpleResponse.class);
        out.write(simpleResponse);

    }

    private BufferState getBufferState(final IoSession ioSession) {
        return (BufferState) ioSession.getAttribute(BUFFER_STATE, BufferState.DOCUMENT_LENGTH);
    }

    private void setBufferState(final IoSession ioSession, final BufferState bufferState) {
        ioSession.setAttribute(BUFFER_STATE, bufferState);
    }

    private byte getMessageType(final IoSession ioSession) {
        return (byte) ioSession.getAttribute(MESSAGE_TYPE, 0);
    }

    private void setMessageType(final IoSession ioSession, byte messageType) {
        ioSession.setAttribute(MESSAGE_TYPE, messageType);
    }

    private Integer getDocumentLength(final IoSession ioSession) {
        return (Integer) ioSession.getAttribute(DOCUMENT_SIZE, 0);
    }

    private void setDocumentLength(final IoSession ioSession, int documentSize) {
        ioSession.setAttribute(DOCUMENT_SIZE, documentSize);
    }

    private void closeWithBadMessage(final Exception ex, final IoSession ioSession) {
        LOG.error("Caught excpetion decoding document.", ex);
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

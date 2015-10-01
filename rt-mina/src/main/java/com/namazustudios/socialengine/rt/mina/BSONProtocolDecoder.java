package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.CorruptEnvelopeException;
import com.namazustudios.socialengine.rt.protocol.Envelope;
import com.namazustudios.socialengine.rt.protocol.EnvelopeFormat;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Decodes data into into BSON objects which can then be handed to the rest of the
 * filter chain.
 *
 * Created by patricktwohig on 7/26/15.
 */
public class BSONProtocolDecoder extends CumulativeProtocolDecoder {

    private static final AttributeKey ENVELOPE = new AttributeKey(BSONProtocolDecoder.class, "Envelope");

    @Inject
    @Named(Constants.BSON_OBJECT_MAPPER)
    private ObjectMapper objectMapper;

    @Inject
    @Named(Constants.MAX_ENVELOPE_SIZE)
    private int maxEnvelopeSize;

    @Inject
    private Provider<Checksum> checksumProvider;

    @Override
    protected boolean doDecode(final IoSession session,
                               final IoBuffer in,
                               final ProtocolDecoderOutput out) throws Exception {

        // First, let's check if there's already an envelope we can work with.

        Envelope envelope = getEnvelope(session);

        // If there is no envelope in the session, then we know that we are waiting

        if (envelope == null) {
            envelope = readEnvelopeHeader(in);
        }

        // Check again.  If we don't have an envelope then we need to wait for more
        // data.

        if (envelope == null) {
            return false;
        }

        // If there is enough in the envelope to read the payload, then we go ahead
        // and read the rest of the payload.

        if (readDocument(session, envelope, in, out)) {
            return true;
        } else {
            // There wasn't enough data to read everything in the envelope, so we're
            // going to let more data accumulate and then try again.  The message is
            // just not complete yet and so we've gotta store the info and wait until
            // later.
            setEnvelope(session, envelope);
            return false;
        }

    }

    private Envelope readEnvelopeHeader(final IoBuffer in) throws IOException {

        if (in.remaining() >= Envelope.envelopeHeaderSize()) {

            final Envelope envelope = new Envelope();
            final IoBuffer envelopeSlice = in.getSlice(envelope.size());
            envelope.setByteBuffer(envelopeSlice.buf(), 0);

            if (envelope.getLength() > (envelope.size() + maxEnvelopeSize)) {
                // This should be caught and the session terminated.  If somebody tries to pass
                // an envelope too large, we just need to kick the can down the road.
                throw new InvalidDataException("size of envelope (" + envelope.getLength() +
                                               ") exceeds max size " + maxEnvelopeSize);
            }

            return envelope;

        } else {
            return null;
        }

    }

    private boolean readDocument(final IoSession ioSession,
                                 final Envelope envelope,
                                 final IoBuffer in,
                                 final ProtocolDecoderOutput out) throws IOException {

        if (in.remaining() >=  envelope.getLength()) {

            try {

                validate(envelope, in);

                final IoBuffer payloadSlice = in.getSlice((int)envelope.getLength());
                final Object message = getMessage(envelope, payloadSlice);
                out.write(message);
                return true;
            } finally {
                ioSession.removeAttribute(ENVELOPE);
            }

        } else {
            return false;
        }

    }

    private void validate(final Envelope envelope,
                          final IoBuffer in) {

        final int position = in.position();
        final long existingChecksum = envelope.getChecksum();

        try {

            envelope.setChecksum(0);

            // Gets a copy of the data from the buffer
            final Checksum checksum = checksumProvider.get();
            final byte[] bytes = new byte[in.position(0).remaining()];
            in.get(bytes);
            checksum.update(bytes, 0, bytes.length);

            if (existingChecksum != checksum.getValue()) {
                throw new CorruptEnvelopeException("checksum for envelopes do not match.");
            }

        } finally {
            in.position(position);
            envelope.setChecksum(existingChecksum);
        }

    }

    private Object getMessage(final Envelope envelope, final IoBuffer payloadSlice) {

        if (!EnvelopeFormat.BSON.equals(envelope.getEnvelopeFormat())) {
            throw new InvalidDataException("envelope format not supported: " + envelope.getEnvelopeFormat());
        }

        final Class<?> messageType;

        switch (envelope.getEnvelopeType()) {
            case REQUEST:
                messageType = SimpleRequest.class;
                break;
            case RESPONSE:
                messageType = SimpleResponse.class;
                break;
            case EVENT:
                messageType = SimpleEvent.class;
                break;
            default:
                throw new InvalidDataException("invalid request envelope type " + envelope.getEnvelopeType());
        }

        try (final InputStream payloadInputStream = payloadSlice.asInputStream()) {
            return objectMapper.readValue(payloadInputStream, messageType);
        } catch (Exception ex) {
            throw new InvalidDataException("could not parse envelope payload", ex);
        }

    }

    private Envelope getEnvelope(final IoSession ioSession) {
        return (Envelope) ioSession.getAttribute(ENVELOPE, null);
    }

    private void setEnvelope(final IoSession ioSession, Envelope envelope) {
        ioSession.setAttribute(ENVELOPE, envelope);
    }

}

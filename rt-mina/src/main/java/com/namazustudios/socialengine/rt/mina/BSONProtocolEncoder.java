package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.protocol.Envelope;
import com.namazustudios.socialengine.rt.protocol.EnvelopeFormat;
import com.namazustudios.socialengine.rt.protocol.EnvelopeType;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.Checksum;

/**
 * Created by patricktwohig on 7/26/15.
 */
public class BSONProtocolEncoder implements ProtocolEncoder {

    private static final int BUFFER_INITIAL_SIZE = 512;

    @Inject
    @Named(Constants.BSON_OBJECT_MAPPER)
    private ObjectMapper objectMapper;

    @Inject
    private Provider<Checksum> checksumProvider;

    @Override
    public void encode(final IoSession session,
                       final Object message,
                       final ProtocolEncoderOutput out) throws Exception {

        if (message instanceof Request) {
            encode((Request)message, out);
        } else if (message instanceof Response) {
            encode((Response)message, out);
        } else if (message instanceof Event) {
            encode((Event) message, out);
        } else {
            throw new IllegalArgumentException("Unexpected message " + message);
        }

    }

    private void encode(final Request request, final ProtocolEncoderOutput out) throws Exception {

        final Envelope.Builder builder = new Envelope.Builder()
            .type(EnvelopeType.REQUEST)
            .format(EnvelopeFormat.BSON);

        encode(builder, request, out);

    }

    private void encode(final Response response, final ProtocolEncoderOutput out) throws Exception {

        final Envelope.Builder builder = new Envelope.Builder()
                .type(EnvelopeType.RESPONSE)
                .format(EnvelopeFormat.BSON);

        encode(builder, response, out);

    }

    private void encode(final Event event, final ProtocolEncoderOutput out) throws Exception {

        final Envelope.Builder builder = new Envelope.Builder()
                .type(EnvelopeType.EVENT)
                .format(EnvelopeFormat.BSON);

        encode(builder, event, out);

    }

    private void encode(final Envelope.Builder builder,
                        final Object payloadToSerialize,
                        final ProtocolEncoderOutput out) throws Exception {

        // Creates an auto-expanding buffer with the initial size.  The buffer should
        // expand as needed
        final IoBuffer ioBuffer = IoBuffer.allocate(BUFFER_INITIAL_SIZE).setAutoExpand(true);
        ioBuffer.position(Envelope.envelopeHeaderSize());

        try (final OutputStream outputStream = ioBuffer.asOutputStream()) {
            objectMapper.writeValue(outputStream, payloadToSerialize);
        }

        // Flips now that we've go all the data in there we want in there which includes the
        // header and the payload.  This marks the limit from which subsequent reads will
        // take place.

        ioBuffer.flip();

        // We now build the envelope header by obtaining the backing byte buffer.

        final ByteBuffer envelopeByteBuffer = ioBuffer.buf();
        final Envelope envelope = builder.build(envelopeByteBuffer);

        // Sets the position back to zero, gets all data back out of the buffer
        // so it can be checksum'd

        final Checksum checksum = checksumProvider.get();
        final byte[] bytes = new byte[ioBuffer.position(0).remaining()];
        ioBuffer.get(bytes);

        // Updates the checksum with the given bytes from the entire buffer.  One calculated,
        // this will update the checksum in the header.  Note this does not break the CRC because
        // the value is zero for the CRC.
        checksum.update(bytes, 0, bytes.length);
        envelope.setChecksum(checksum.getValue());

        // Finally flips the buffer over to be read again
        ioBuffer.flip();
        out.write(ioBuffer);

    }

    @Override
    public void dispose(IoSession session) throws Exception {}

}

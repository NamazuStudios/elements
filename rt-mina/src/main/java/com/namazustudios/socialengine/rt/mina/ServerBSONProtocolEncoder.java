package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by patricktwohig on 7/26/15.
 */
public class ServerBSONProtocolEncoder implements ProtocolEncoder {

    @Inject
    @Named(Constants.BSON_OBJECT_MAPPER)
    private ObjectMapper objectMapper;

    @Override
    public void encode(final IoSession session,
                       final Object message,
                       final ProtocolEncoderOutput out) throws Exception {

        if (message instanceof Response) {
            encode((Response)message, out);
        } else if (message instanceof Event) {
            encode((Event) message, out);
        } else {
            throw new IllegalArgumentException("Unexpected message " + message);
        }

    }

    private void encode(final Response response, final ProtocolEncoderOutput out) throws Exception {

        final IoBuffer ioBuffer;
        final SimpleResponse simpleResponse = SimpleResponse.builder().from(response).build();

        try (final IoBufferByteArrayOutputStream byteArrayOutputStream = new IoBufferByteArrayOutputStream()) {
            byteArrayOutputStream.write(BSONMessageType.RESPONSE.getCode());
            objectMapper.writeValue(byteArrayOutputStream, simpleResponse);
            ioBuffer = byteArrayOutputStream.toIoBuffer();
        }

        out.write(ioBuffer);

    }

    private void encode(final Event event, final ProtocolEncoderOutput out) throws Exception {

        final IoBuffer ioBuffer;
        final SimpleEvent simpleEvent = SimpleEvent.builder().event(event).build();

        try (final IoBufferByteArrayOutputStream byteArrayOutputStream = new IoBufferByteArrayOutputStream()) {
            byteArrayOutputStream.write(BSONMessageType.EVENT.getCode());
            objectMapper.writeValue(byteArrayOutputStream, event);
            ioBuffer = byteArrayOutputStream.toIoBuffer();
        }

        out.write(ioBuffer);

    }

    @Override
    public void dispose(IoSession session) throws Exception {}

}

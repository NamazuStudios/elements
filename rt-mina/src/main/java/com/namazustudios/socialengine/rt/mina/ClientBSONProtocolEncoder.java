package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.SimpleResponse;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by patricktwohig on 9/11/15.
 */
public class ClientBSONProtocolEncoder implements ProtocolEncoder {

    @Inject
    @Named(Constants.BSON_OBJECT_MAPPER)
    private ObjectMapper objectMapper;

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {

        if (message instanceof Request) {
            encode((Request)message, out);
        } else {
            throw new IllegalArgumentException("Invalid message " + message);
        }

    }

    private void encode(final Request request, final ProtocolEncoderOutput out) throws Exception {

        final IoBuffer ioBuffer;
        final SimpleResponse simpleResponse = SimpleResponse.builder().from(request).build();

        try (final IoBufferByteArrayOutputStream byteArrayOutputStream = new IoBufferByteArrayOutputStream()) {
            byteArrayOutputStream.write(BSONMessageType.RESPONSE.getCode());
            objectMapper.writeValue(byteArrayOutputStream, simpleResponse);
            ioBuffer = byteArrayOutputStream.toIoBuffer();
        }

        out.write(ioBuffer);

    }

    @Override
    public void dispose(IoSession session) throws Exception {}

}

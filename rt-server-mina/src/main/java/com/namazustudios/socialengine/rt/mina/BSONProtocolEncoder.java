package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.Response;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

/**
 * Created by patricktwohig on 7/26/15.
 */
public class BSONProtocolEncoder implements ProtocolEncoder {

    @Inject
    private ObjectMapper objectMapper;

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {

        final Response response = (Response)message;
        final Object payload = response.getPayload();

        final IoBuffer ioBuffer;

        try (final IoBufferByteArrayOutputStream byteArrayOutputStream = new IoBufferByteArrayOutputStream()) {
            objectMapper.writeValue(byteArrayOutputStream, response);
            objectMapper.writeValue(byteArrayOutputStream, payload);
            ioBuffer = byteArrayOutputStream.toIoBuffer();
        }

        out.write(ioBuffer);

    }

    @Override
    public void dispose(IoSession session) throws Exception {}

    private static class IoBufferByteArrayOutputStream extends ByteArrayOutputStream {

        public IoBuffer toIoBuffer() {
            final IoBuffer ioBuffer = IoBuffer.allocate(count, false);
            ioBuffer.put(buf, 0, count);
            ioBuffer.flip();
            return ioBuffer;
        }

    }

}

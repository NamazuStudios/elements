package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.ResponseHeader;
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
    public void encode(final IoSession session,
                       final Object message,
                       final ProtocolEncoderOutput out) throws Exception {

        if (message instanceof Response) {
            encodeRespones((Response)message, out);
        } else {
            throw new IllegalArgumentException("Unexpected message " + message);
        }

    }

    private void encodeRespones(final Response response, final ProtocolEncoderOutput out) throws Exception {

        final IoBuffer ioBuffer;

        final ResponseHeader responseHeader = response.getResponseHeader();
        final Object payload = response.getPayload();

        try (final IoBufferByteArrayOutputStream byteArrayOutputStream = new IoBufferByteArrayOutputStream()) {
            objectMapper.writeValue(byteArrayOutputStream, responseHeader);
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

package com.namazustudios.socialengine.rt.mina;

import org.apache.mina.core.buffer.IoBuffer;

import java.io.ByteArrayOutputStream;

/**
 * Created by patricktwohig on 9/11/15.
 */
class IoBufferByteArrayOutputStream extends ByteArrayOutputStream {

    public IoBuffer toIoBuffer() {
        final IoBuffer ioBuffer = IoBuffer.allocate(count, false);
        ioBuffer.put(buf, 0, count);
        ioBuffer.flip();
        return ioBuffer;
    }

}

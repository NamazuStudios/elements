package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.id.ResourceId;
import jetbrains.exodus.ByteIterable;

import java.nio.ByteBuffer;

import static java.lang.System.arraycopy;

public class XodusUtil {

    static ResourceId resourceId(final ByteIterable byteIterable) {
        final var ridBytes = new byte[byteIterable.getLength()];
        arraycopy(byteIterable.getBytesUnsafe(), 0, ridBytes, 0, ridBytes.length);
        return ResourceId.resourceIdFromBytes(ridBytes);
    }

    static ByteBuffer byteBuffer(final ByteIterable byteIterable) {
        return ByteBuffer.wrap(byteIterable.getBytesUnsafe(), 0, byteIterable.getLength());
    }

}

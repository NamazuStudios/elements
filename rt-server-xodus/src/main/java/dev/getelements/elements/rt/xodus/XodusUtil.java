package dev.getelements.elements.rt.xodus;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.rt.id.ResourceId;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteBufferByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.util.ByteIterableUtil;

import java.nio.ByteBuffer;
import java.util.Iterator;

import static java.lang.Integer.min;
import static java.lang.System.arraycopy;
import static java.nio.ByteOrder.BIG_ENDIAN;

public class XodusUtil {

    /**
     * Gets a {@link Path} from the supplied {@link ByteIterable}
     *
     * @param byteIterable the {@link ByteIterable}
     * @return the {@link Path}
     */
    static Path path(final ByteIterable byteIterable) {

        final var pathBytes = new byte[byteIterable.getLength()];

        try {
            final var unsafe = byteIterable.getBytesUnsafe();
            arraycopy(unsafe, 0, pathBytes, 0, pathBytes.length);
        } catch (UnsupportedOperationException ex) {
            int idx = 0;
            final var itr = byteIterable.iterator();
            while (itr.hasNext()) pathBytes[idx++] = itr.next();
        }

        return Path.fromBytes(pathBytes);

    }

    /**
     * Gets a {@link ResourceId} from the supplied {@link ByteIterable}
     *
     * @param byteIterable the {@link ByteIterable}
     * @return the {@link ResourceId}
     */
    static ResourceId resourceId(final ByteIterable byteIterable) {

        final var ridBytes = new byte[ResourceId.getSizeInBytes()];

        try {
            final var unsafe = byteIterable.getBytesUnsafe();
            arraycopy(unsafe, 0, ridBytes, 0, min(ridBytes.length, byteIterable.getLength()));
        } catch (UnsupportedOperationException ex) {
            int idx = 0;
            final var itr = byteIterable.iterator();
            while (itr.hasNext() && idx < ResourceId.getSizeInBytes()) ridBytes[idx++] = itr.next();
        }

        return ResourceId.resourceIdFromBytes(ridBytes);

    }

    /**
     * Gets a {@link ByteBuffer} from the supplied {@link ByteIterable}.
     *
     * @param byteIterable the {@link ByteIterable}
     * @return the {@link ByteBuffer}
     */
    static ByteBuffer byteBuffer(final ByteIterable byteIterable) {
        try {
            final var unsafe = byteIterable.getBytesUnsafe();
            return ByteBuffer.wrap(unsafe, 0, byteIterable.getLength()).asReadOnlyBuffer();
        } catch (UnsupportedOperationException ex) {
            final var buffer = ByteBuffer.allocate(byteIterable.getLength());
            final var itr = byteIterable.iterator();
            while (itr.hasNext()) buffer.put(itr.next());
            return buffer.asReadOnlyBuffer();
        }
    }

    /**
     * Gets a {@link ByteIterable} which represents the supplied {@link ResourceId}.
     *
     * @param resourceId
     * @return
     */
    static ByteIterable resourceIdKey(final ResourceId resourceId) {
        final var bytes = resourceId.asBytes();
        return new ArrayByteIterable(bytes);
    }

    /**
     * Gets the key for a resource block.
     *
     * @param resourceId the {@link ResourceId}
     * @param blockSequence the block sequence
     *
     * @return the key for the resource id and block sequence
     */
    static ByteIterable resourceBlockKey(final ResourceId resourceId, final long blockSequence) {
        final var buffer = ByteBuffer.allocate(ResourceId.getSizeInBytes() + Integer.BYTES);
        buffer.order(BIG_ENDIAN);
        buffer.put(resourceId.asBytes());
        buffer.putInt( (int) blockSequence);
        buffer.flip();
        return new ByteBufferByteIterable(buffer);
    }

    /**
     * Gets a key for the {@link Path}.
     *
     * @param path the {@link Path}
     * @return the key for the {@link Path}
     */
    static ByteIterable pathKey(final Path path) {
        return new ArrayByteIterable(path.toByteArray());
    }

    /**
     * Checks if two {@link ByteIterable}s match a {@link ResourceId} to one of its blocks.
     *
     * @param resourceIdKey the resource id key
     * @param resourceBlockKey the block key
     * @return true if the supplied block key belongs to that resource
     */
    static boolean isMatchingBlockKey(final ByteIterable resourceIdKey, final ByteIterable resourceBlockKey) {
        var ktyItr = resourceIdKey.iterator();
        var blockKeyItr = resourceBlockKey.iterator();
        while (blockKeyItr.hasNext() && ktyItr.hasNext()) if (ktyItr.next() != blockKeyItr.next()) break;
        return !ktyItr.hasNext();
    }

    /**
     * Checks if two {@link ByteIterable} isntances are equal in contents.
     *
     * @param l the left side
     * @param r the right side
     * @return true if equal, false otherwise
     */
    static boolean isEqual(final ByteIterable l, final ByteIterable r) {
        return (l == r) || ByteIterableUtil.compare(l, r) == 0;
    }

    /**
     * Checks if two {@link ByteIterable} isntances are equal in contents.
     *
     * @param l the left side (may be null)
     * @param r the right side (may be null)
     * @return true if equal, false otherwise
     */
    static boolean isEqualNullSafe(final ByteIterable l, final ByteIterable r) {
        return (l == r) || (l != null && r != null && isEqual(l, r));
    }

}

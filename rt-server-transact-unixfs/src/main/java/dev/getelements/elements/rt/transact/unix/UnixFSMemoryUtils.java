package dev.getelements.elements.rt.transact.unix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;

import static java.lang.invoke.MethodHandles.byteArrayViewVarHandle;
import static java.lang.invoke.MethodHandles.byteBufferViewVarHandle;
import static java.nio.ByteOrder.nativeOrder;

/**
 * Some dirty hacks to support atomic memory access.
 */
public abstract class UnixFSMemoryUtils {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSMemoryUtils.class);

    private static final UnixFSMemoryUtils instance = new SaveUnixFSMemoryUtils();

    /**
     * Gets the instance of the {@link UnixFSMemoryUtils} based on the system configuration.
     *
     * @return the instance of {@link UnixFSMemoryUtils}
     */
    public static UnixFSMemoryUtils getInstance() {
        return instance;
    }

    /**
     * Makes an instance of {@link UnixFSAtomicLong} from
     * {@see {@link UnixFSMemoryUtils#getAtomicLong(ByteBuffer, int)}} for details.
     */
    public UnixFSAtomicLong getAtomicLong(final ByteBuffer byteBuffer) {

        final int position = byteBuffer.position();
        final UnixFSAtomicLong counter = getAtomicLong(byteBuffer, position);

        final long value = byteBuffer.getLong();
        logger.trace("Created {} with initial value {}, ", counter, value);

        return counter;

    }

    /**
     * Gets a {@link UnixFSAtomicLong} with the supplied {@link ByteBuffer}. The 64-bit counter object will be
     * placed at the {@link ByteBuffer}'s {@link ByteBuffer#position()}.
     *
     * The {@link ByteBuffer}'s position is unchanged.
     *
     * This presumes that the {@link ByteBuffer#position()} is properly aligned for this type of atomic operation and
     * if it is not, then undefined behavior may result.
     *
     * @param byteBuffer the {@link ByteBuffer}
     * @param position the positing within the {@link ByteBuffer}
     * @return the {@link UnixFSAtomicLong}
     * @throws IllegalArgumentException if this method can detect misalignment, or otherwise improper use
     */
    public abstract UnixFSAtomicLong getAtomicLong(final ByteBuffer byteBuffer, final int position);

    private static class SaveUnixFSMemoryUtils extends UnixFSMemoryUtils {
        private static final VarHandle LONG_VAR_HANDLE = byteBufferViewVarHandle(long[].class, nativeOrder());

        @Override
        public UnixFSAtomicLong getAtomicLong(final ByteBuffer byteBuffer, final int position) {

            final var nativeByteOrder = nativeOrder().equals(byteBuffer.order());
            final ByteOrderCorrection byteOrderCorrection = nativeByteOrder ? l -> l : Long::reverseBytes;

            if ((position % Long.BYTES) != 0) {
                throw new IllegalArgumentException("Misaligned position: " + position);
            }

            if ((byteBuffer.capacity() - (Long.BYTES + position)) < 0) {
                throw new IllegalArgumentException("Position out of bounds:" + position);
            }

            return new UnixFSAtomicLong() {
                @Override
                public long get() {
                    final var value = LONG_VAR_HANDLE.getVolatile(byteBuffer, position);
                    return byteOrderCorrection.correctByteOrder((Long) value);
                }

                @Override
                public boolean compareAndSet(long expect, long update) {
                    final long expectCorrected = byteOrderCorrection.correctByteOrder(expect);
                    final long updateCorrected = byteOrderCorrection.correctByteOrder(update);
                    return LONG_VAR_HANDLE.compareAndSet(byteBuffer, position, expectCorrected, updateCorrected);
                }
            };
        }

    }

    @FunctionalInterface
    private interface ByteOrderCorrection {
        long correctByteOrder(long input);
    }

}

package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.FatalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.function.LongSupplier;

import static java.lang.String.format;

/**
 * Some dirty hacks to support atomic memory access.
 */
public abstract class UnixFSMemoryUtils {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSMemoryUtils.class);

    private static final UnixFSMemoryUtils instance = new UnsafeUnixFSMemoryUtils();

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
     * @throws {@link IllegalArgumentException} if this method can detect misalignment, or otherwise improper use
     */
    public abstract UnixFSAtomicLong getAtomicLong(final ByteBuffer byteBuffer, final int position);

    private static class UnsafeUnixFSMemoryUtils extends UnixFSMemoryUtils {

        private final Unsafe unsafe;

        private final Field byteBufferAddressField;

        private final Field byteBufferNativeOrderField;

        public UnsafeUnixFSMemoryUtils() {
            try {
                unsafe = loadUnsafe();
                byteBufferAddressField = loadByteBufferAddressField();
                byteBufferNativeOrderField = loadByteBuffferNativeOrderField();
            } catch (Exception ex) {
                throw new InternalException(ex);
            }
        }

        private Unsafe loadUnsafe() throws NoSuchFieldException, IllegalAccessException {
            final Field fUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            fUnsafe.setAccessible(true);
            return  (Unsafe)fUnsafe.get(null);
        }

        private final Field loadByteBufferAddressField() throws ClassNotFoundException, NoSuchFieldException {
            final Class<?> javaNioDirectByteBufferClass = Class.forName("java.nio.Buffer");
            final Field fAddress = javaNioDirectByteBufferClass.getDeclaredField("address");
            fAddress.setAccessible(true);
            return fAddress;
        }

        private final Field loadByteBuffferNativeOrderField() throws ClassNotFoundException, NoSuchFieldException {
            final Class<?> javaNioDirectByteBufferClass = Class.forName("java.nio.ByteBuffer");
            final Field fNativeOrder = javaNioDirectByteBufferClass.getDeclaredField("nativeByteOrder");
            fNativeOrder.setAccessible(true);
            return fNativeOrder;
        }

        @Override
        public UnixFSAtomicLong getAtomicLong(final ByteBuffer byteBuffer, final int position) {

            if (byteBuffer.isReadOnly()) throw new IllegalArgumentException("Read-only buffers not supported.");
            if (!byteBuffer.isDirect()) throw new IllegalArgumentException("Only direct bytebuffers are supported.");
            if ((position + Long.BYTES) > byteBuffer.limit()) throw new IllegalArgumentException("Not enough space in buffer");

            final DirectBuffer directBuffer = (DirectBuffer) byteBuffer;

            try {

                // Determines if we have to flip the byte buffer order around
                final boolean nativeByteOrder = byteBufferNativeOrderField.getBoolean(byteBuffer);
                final ByteOrderCorrection byteOrderCorrection = nativeByteOrder ? l -> l : Long::reverseBytes;

                final LongSupplier memoryAddressSupplier = () -> {
                    try {
                        final long base = byteBufferAddressField.getLong(byteBuffer);
                        return base + position;
                    } catch (IllegalAccessException ex) {
                        logger.error("Unable to find memory address of counter.", ex);
                        throw new FatalException(ex);
                    }
                };

                final long address = memoryAddressSupplier.getAsLong();
                final long misalignment = address % Long.BYTES;

                if (misalignment != 0) {

                    final String msg = format(
                        "Address of long 0x%X is     not properly aligned. Misalignment of %d bytes.",
                        address, misalignment
                    );

                    throw new IllegalArgumentException(msg);

                }

                return new UnixFSAtomicLong() {
                    @Override
                    public long get() {
                        final long address = memoryAddressSupplier.getAsLong();
                        final long raw = unsafe.getLongVolatile(null, address);
                        final long corrected = byteOrderCorrection.correctByteOrder(raw);
                        return corrected;
                    }

                    @Override
                    public boolean compareAndSet(final long expect, final long update) {

                        final long address = memoryAddressSupplier.getAsLong();
                        final long expectCorrected = byteOrderCorrection.correctByteOrder(expect);
                        final long updateCorrected = byteOrderCorrection.correctByteOrder(update);

                        try {
                            return unsafe.compareAndSwapLong(null, address, expectCorrected, updateCorrected);
                        } finally {
                            unsafe.storeFence();
                        }

                    }

                    @Override
                    public String toString() {
                        return format("Unsafe counter %s (pos=%d)", byteBuffer, position);
                    }

                };

            } catch (IllegalAccessException e) {
                throw new InternalException(e);
            }

        }

    }

    @FunctionalInterface
    private interface ByteOrderCorrection {
        long correctByteOrder(long input);
    }

}

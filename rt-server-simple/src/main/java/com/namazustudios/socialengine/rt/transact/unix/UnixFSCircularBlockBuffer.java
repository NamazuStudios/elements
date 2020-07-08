package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.FatalException;
import javolution.io.Struct;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.fill;

/**
 * Given a {@link ByteBuffer}, this slices the buffer into a series of smaller {@link ByteBuffer}s which represent a
 * subset of the data. This allows a {@link ByteBuffer} to be used by several threads w/ exclusive access to subsections
 * of the content.
 */
public class UnixFSCircularBlockBuffer {

    private final byte[] filler;

    private final UnixFSDualCounter counter;

    private final List<ByteBuffer> slices = new ArrayList<>();

    public UnixFSCircularBlockBuffer(final ByteBuffer buffer, final int blockSize) {

        final ByteBuffer toSlice = buffer.slice();

        if ((toSlice.remaining() % blockSize) != 0) {
            throw new IllegalArgumentException("Invalid block size: " + blockSize);
        }

        final int sliceCount = toSlice.remaining() / blockSize;
        counter = new UnixFSDualCounter(sliceCount);

        this.filler = new byte[blockSize];
        fill(filler, (byte) 0xFF);

        for (int sliceIndex = 0; sliceIndex < sliceCount; ++sliceIndex) {
            final int position = sliceIndex * blockSize;
            toSlice.position(position).limit(position + blockSize);
            slices.add(toSlice.slice());
        }

    }

    /**
     * Gets the current slice, the returned {@link Slice} should be closed, but does not return the {@link Slice} to the
     * pool of slices.
     *
     * @return the current slice
     */
    public ByteBuffer peek() {
        if (counter.isEmpty()) throw new IllegalStateException("No current slice.");
        final int leading = counter.getLeading();
        return slices.get(leading);
    }

    /**
     * Gets the next {@link Slice}, which will be held by the caller until the returned slice is closed.
     *
     * @return the next {@link Slice}
     * @throws FatalException if the buffer has been exhausted
     */
    public Slice<ByteBuffer> next() {

        final int leading = counter.incrementAndGetLeading();
        final ByteBuffer slice = slices.get(leading);

        return new Slice() {

            @Override
            public ByteBuffer getSlice() {
                return slice;
            }

            @Override
            public void close() {
                slice.clear();
                slice.put(filler);
                counter.incrementAndGetTrailing();
            }

        };
    }

    /**
     * Allows for the creation of a {@link StructTypedView<StructT>} which can represent the underlying data slices
     * as a fixed-sized array of {@link Struct} types.
     *
     * @param structSuppler the {@link StructT} supplier
     * @param <StructT> the type of the struct
     * @return the new typed view
     */
    public <StructT extends Struct> StructTypedView<StructT> forStructType(final Supplier<StructT> structSuppler) {
        return new StructTypedView<StructT>(structSuppler);
    }

    /**
     * Represents a slice of the provided byte buffer array.
     */
    interface Slice<T> extends AutoCloseable {

        /**
         * Gets the actual {@link ByteBuffer} associated slice.
         *
         * @return the {@link ByteBuffer}
         */
        T getSlice();

        /**
         * Returns the {@link Slice} to the pool if necessary.
         */
        default void close() {}

    }

    /**
     * Represents a view of this as a cached array of {@link Struct} objects. Mutations to this object are reflected
     * in the original object, and vise-versa. This caches each {@link Struct} instance such that it may be
     * automatically recycled and used later.
     *
     * @param <StructT>
     */
    class StructTypedView<StructT extends Struct> {

        private final List<StructT> structs = new ArrayList<>();

        private StructTypedView(final Supplier<StructT> structSupplier) {
            for (final ByteBuffer buffer : slices) {
                final StructT struct = structSupplier.get();
                struct.setByteBuffer(buffer, 0);
            }
        }

        /**
         * Functionally similar to {@link UnixFSCircularBlockBuffer#peek()}.
         *
         * @return the current struct object
         */
        StructT peek() {
            final int index = counter.getLeading();
            return structs.get(index);
        }

        /**
         * Functionally similar to {@link UnixFSCircularBlockBuffer#next()}.
         *
         * @return the next struct object
         */
        Slice<StructT> next() {

            final int index = counter.incrementAndGetLeading();

            return new Slice<StructT>() {

                @Override
                public StructT getSlice() {
                    return structs.get(index);
                }

                @Override
                public void close() {
                    final ByteBuffer slice = slices.get(index);
                    slice.clear();
                    slice.put(filler);
                    counter.incrementAndGetTrailing();
                }

            };
        }

    }

}

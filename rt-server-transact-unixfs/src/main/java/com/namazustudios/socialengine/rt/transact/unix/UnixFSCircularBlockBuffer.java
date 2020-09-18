package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.fill;

/**
 * Given a {@link ByteBuffer}, this slices the buffer into a series of smaller {@link ByteBuffer}s which represent a
 * subset of the data. This allows a {@link ByteBuffer} to be used by several threads w/ exclusive access to subsections
 * of the content.
 *
 * While this structure's behavior is thread safe, care must be taken by the client code to ensure that the data
 * contained in the block buffer is mutated appropriately.
 *
 * The {@link #stream()} {@link #reverse()} methods of this class uses a snapshot of the trailing/leading values and it
 * is possible that data has changed while iterating. Therefore, code using an iterator must be prepared to handle such
 * scenarios.
 */
public class UnixFSCircularBlockBuffer {

    private final byte[] filler;

    private final UnixFSDualCounter counter;

    private final List<ByteBuffer> slices = new ArrayList<>();

    public UnixFSCircularBlockBuffer(final UnixFSAtomicLong atomicLong,
                                     final ByteBuffer buffer,
                                     final int blockSize) {

        final ByteBuffer toSlice = buffer.slice();

        if ((toSlice.remaining() % blockSize) != 0) {
            throw new IllegalArgumentException("Invalid block size: " + blockSize);
        }

        final int sliceCount = toSlice.remaining() / blockSize;
        this.counter = new UnixFSDualCounter(sliceCount - 1, atomicLong);

        this.filler = new byte[blockSize];
        fill(filler, (byte) 0xFF);

        for (int sliceIndex = 0; sliceIndex < sliceCount; ++sliceIndex) {
            final int position = sliceIndex * blockSize;
            toSlice.position(position).limit(position + blockSize);
            slices.add(toSlice.slice());
        }

    }

    /**
     * Resets the {@link UnixFSCircularBlockBuffer}
     *
     * @return this instance
     */
    public UnixFSCircularBlockBuffer reset() {
        counter.reset();
        return this;
    }

    /**
     * Returns true if all space is available in the buffer.
     *
     * @return true, if empty. false, otherwise
     */
    public boolean isEmpty() {
        return counter.isEmpty();
    }

    /**
     * Returns true if there is no space left in the buffer.
     *
     * @return true if no space is left
     */
    public boolean isFull() {
        return counter.isFull();
    }

    /**
     * Increments the current leading value, and returns the {@link ByteBuffer} associated with the value.
     *
     * @return the next leading value.
     */
    public Slice<ByteBuffer> nextLeading() {
        final int leading = counter.incrementLeadingAndGet();
        return new Slice<>(leading, slices.get(leading), slices.get(leading));
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
        return new StructTypedView<>(structSuppler);
    }

    /**
     * Returns a stream of the {@link UnixFSCircularBlockBuffer} in order, trailing to leading.
     *
     * @return the {@link Stream<ByteBuffer>}
     */
    public Stream<Slice<ByteBuffer>> stream() {
        final UnixFSDualCounter.Snapshot snapshot =  counter.getSnapshot();
        return snapshot.range().mapToObj(i -> new Slice(i, slices.get(i), slices.get(i)));
    }

    /**
     * Streams the contents of this {@link UnixFSCircularBlockBuffer} in reverse order, leading to trailing.
     *
     * @return the {@link Stream<ByteBuffer>}
     */
    public Stream<Slice<ByteBuffer>> reverse() {
        final UnixFSDualCounter.Snapshot snapshot =  counter.getSnapshot();
        return snapshot.reverseRange().mapToObj(i -> new Slice(i, slices.get(i), slices.get(i)));
    }

    /**
     * Represents a slice of the provided byte buffer array. Two {@link Slice<T>} instances are equal if they both
     * point to the same index in the {@link UnixFSCircularBlockBuffer}, as they essentially point to the same data.
     * This may be a little counterintuitive as two {@link Slice<T>} instances of differing generic types may be the
     * same even if they house different values.
     */
    public class Slice<T> implements Comparable<Slice<?>> {

        private final int index;

        private final T value;

        private final ByteBuffer buffer;

        private final UnixFSCircularBlockBuffer owner = UnixFSCircularBlockBuffer.this;

        private Slice(int index, T value, ByteBuffer buffer) {
            this.index = index;
            this.value = value;
            this.buffer = buffer;
        }

        /**
         * Gets the actual {@link ByteBuffer} associated slice.
         *
         * @return the {@link ByteBuffer}
         */
        T getValue() {
            return value;
        }

        /**
         * Clears the contents of the slice by filling the underlying memory with the filler bytes.
         */
        public Slice<T> clear() {
            buffer.position(0).limit(filler.length);
            buffer.put(filler);
            return this;
        }

        @Override
        public int compareTo(final Slice<?> o) {
            if (owner != o.owner) throw new IllegalArgumentException("Can only compare two Slices with same owner.");
            return index - o.index;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Slice<?> slice = (Slice<?>) o;
            if (owner != slice.owner) return false;

            return index == slice.index;

        }

        @Override
        public int hashCode() {
            return index;
        }

        public <U> U map(final Function<T, U> mapper) {
            return mapper.apply(getValue());
        }

        public <U> Slice<U> flatMap(final Function<T, U> mapper) {
            final U value = mapper.apply(getValue());
            return new Slice<U>(index, value, buffer);
        }

    }

    @Override
    public String toString() {
        return "UnixFSCircularBlockBuffer{" +
                "filler=" + Arrays.toString(filler) +
                ", counter=" + counter +
                ", slices=" + slices +
                '}';
    }

    /**
     * Represents a view of this as a cached array of {@link Struct} objects. Mutations to this object are reflected
     * in the original object, and vise-versa. This caches each {@link Struct} instance such that it may be
     * automatically recycled and used later.
     *
     * @param <StructT>
     */
    public class StructTypedView<StructT extends Struct> {

        private final List<StructT> structs = new ArrayList<>();

        private StructTypedView(final Supplier<StructT> structSupplier) {
            for (final ByteBuffer buffer : slices) {
                final StructT struct = structSupplier.get();
                struct.setByteBuffer(buffer, 0);
                structs.add(struct);
            }
        }

        /**
         * Returns a stream view of the current valid elements contained in this buffer.
         *
         * @return the {@link Stream<StructT>}
         */
        public Stream<Slice<StructT>> stream() {
            final UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();
            return snapshot
                .range()
                .mapToObj(i -> new Slice<>(i, structs.get(i), slices.get(i)));
        }

        /**
         * Returns a stream view of the current valid elements contained in this buffer, in reverse order.
         *
         * @return the {@link Stream<StructT>}
         */
        public Stream<Slice<StructT>> reverse() {
            final UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();
            return snapshot
                .reverseRange()
                .mapToObj(i -> new Slice<>(i, structs.get(i), slices.get(i)));
        }

        /**
         * Increments the
         * @return
         */
        public Slice<StructT> nextLeading() {
            final int leading = counter.incrementLeadingAndGet();
            return new Slice<>(leading, structs.get(leading), slices.get(leading));
        }

        public void reset() {
            counter.reset();
        }

    }

}

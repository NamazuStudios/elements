package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.RevisionDataStore;
import javolution.io.Struct;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.fill;

/**
 * Given a {@link ByteBuffer}, this slices the buffer into a series of smaller {@link ByteBuffer}s which represent a
 * subset of the data. This allows a {@link ByteBuffer} to be used by several threads w/ exclusive access to subsections
 * of the content.
 *
 * While this structure's behavior is thread safe, care must be taken by the client code to ensure that the data
 * contained in the block buffer is mutated appropriately. Specifically each {@link Slice} instance returend by this
 * {@link UnixFSCircularBlockBuffer} is not thread safe. Therefore, additional steps must be taken to ensure predictable
 * behavior when dealing with the individual {@link Slice}s of this buffer.
 *
 * The {@link View#stream()} {@link View#reverse()} methods of this class uses a snapshot of the trailing/leading values
 * and it is possible that data has changed while iterating. Therefore, code using an iterator must be prepared to
 * handle such scenarios.
 *
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
     * Returns a raw-view of the circular block buffer.
     *
     * @return the raw view
     */
    public View<ByteBuffer> rawView() {
        return new View<ByteBuffer>() {

            @Override
            public boolean isEmpty() {
                return counter.isEmpty();
            }

            @Override
            public boolean isFull() {
                return counter.isFull();
            }

            @Override
            public int size() {
                return counter.size();
            }

            @Override
            public Stream<Slice<ByteBuffer>> stream() {
                final UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();
                return snapshot.range().mapToObj(UnixFSCircularBlockBuffer.this::rawSliceAt);
            }

            @Override
            public Stream<Slice<ByteBuffer>> reverse() {
                final UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();
                return snapshot.reverseRange().mapToObj(UnixFSCircularBlockBuffer.this::rawSliceAt);
            }

            @Override
            public Slice<ByteBuffer> nextLeading() {
                final int leading = counter.incrementLeadingAndGet();
                return rawSliceAt(leading);
            }

            @Override
            public Slice<ByteBuffer> get(int index) {
                final UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();
                if (!snapshot.inRange(index)) throw new IllegalArgumentException(index + " is out of range " + snapshot);
                return rawSliceAt(index);
            }

            @Override
            public void incrementTrailingUntil(final Predicate<Slice<ByteBuffer>> predicate) {

                UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();

                while (!snapshot.isEmpty()) {

                    final int trailing = snapshot.getTrailing();
                    final Slice<ByteBuffer> trailingSlice = rawSliceAt(trailing);

                    if (predicate.test(trailingSlice)) {
                        counter.compareAndIncrementTrailing(snapshot);
                        snapshot = counter.getSnapshot();
                    } else {
                        break;
                    }

                }

            }

            @Override
            public View<ByteBuffer> reset() {
                counter.reset();
                return this;
            }

        };
    }

    /**
     * Allows for the creation of a {@link View<StructT>} which can represent the underlying data slices
     * as a fixed-sized array of {@link Struct} types.
     *
     * @param structSuppler the {@link StructT} supplier
     * @param <StructT> the type of the struct
     * @return the new typed view
     */
    public <StructT extends Struct> View<StructT> forStructType(final Supplier<StructT> structSuppler) {
        return rawView().flatMap(bb -> {
            final StructT struct = structSuppler.get();
            struct.setByteBuffer(bb, 0);
            return struct;
        });
    }

    private ByteBuffer rawBufferAt(final int index) {
        // Defensively, we take a duplicate of the slice so that multiple threads may observe or manipulate the
        // underlying data without but without having worry about breaking the associated buffer's limit and offset.
        final ByteBuffer slice = slices.get(index).duplicate();
        return slice;
    }

    private Slice<ByteBuffer> rawSliceAt(final int index) {
        final ByteBuffer slice = rawBufferAt(index);
        return new Slice<>(index, slice, slice);
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
     * Represents a view of the underlying {@link UnixFSCircularBlockBuffer}. Unless otherwise stated, all operations
     * which mutate this View, will also mutate the underling parent buffer.
     *
     * @param <ViewedT>
     */
    public interface View<ViewedT> {

        /**
         * Returns true if all space is available in the buffer.
         *
         * @return true, if empty. false, otherwise
         */
        boolean isEmpty();

        /**
         * Returns true if there is no space left in the buffer.
         *
         * @return true if no space is left
         */
        boolean isFull();

        /**
         * Returns the size of the block buffer.
         *
         * @return the size
         */
        int size();

        /**
         * Returns a stream view of the current valid elements contained in this buffer.
         *
         * @return the {@link Stream<ViewedT>}
         */
        Stream<Slice<ViewedT>> stream();

        /**
         * Returns a stream view of the current valid elements contained in this buffer, in reverse order.
         *
         * @return the {@link Stream<ViewedT>}
         */
        Stream<Slice<ViewedT>> reverse();

        /**
         * Increments the leading and gets the next object in the view.
         * @return
         */
        Slice<ViewedT> nextLeading();

        /**
         * Gets the {@link Slice<ViewedT>} associated with the index provided that the index is valid and within
         * the range of the circular block buffer.
         *
         * @param index the index
         * @return the {@link Slice<ViewedT>}
         */
        Slice<ViewedT> get(int index);

        /**
         * Resets this {@link View<ViewedT>}.
         *
         * @return this instance
         */
        View<ViewedT> reset();

        /**
         * Increments the trailing value, evaluating each {@link Slice<ViewedT>}
         *
         * @param predicate
         */
        void incrementTrailingUntil(Predicate<Slice<ViewedT>> predicate);

        /**
         * Transforms this type of {@link View<View>} to an instance of {@link View<OtherT>}.
         *
         * @param transform the transform function to apply.
         * @param <OtherT> the other type
         *
         * @return another {@link View<OtherT>}
         */
        default <OtherT> View<OtherT> flatMap(final Function<ViewedT, OtherT> transform) {

            return new View<OtherT>() {
                @Override
                public boolean isEmpty() {
                    return View.this.isEmpty();
                }

                @Override
                public boolean isFull() {
                    return View.this.isFull();
                }

                @Override
                public int size() {
                    return View.this.size();
                }

                @Override
                public Stream<Slice<OtherT>> stream() {
                    return View.this.stream().map(s -> s.map(transform));
                }

                @Override
                public Stream<Slice<OtherT>> reverse() {
                    return View.this.reverse().map(s -> s.map(transform));
                }

                @Override
                public Slice<OtherT> nextLeading() {
                    return View.this.nextLeading().map(transform);
                }

                @Override
                public Slice<OtherT> get(final int index) {
                    return View.this.get(index).map(transform);
                }

                @Override
                public void incrementTrailingUntil(final Predicate<Slice<OtherT>> predicate) {
                    View.this.incrementTrailingUntil(s -> {
                        final Slice<OtherT> other = s.map(transform);
                        return predicate.test(other);
                    });
                }

                @Override
                public View<OtherT> reset() {
                    View.this.reset();
                    return this;
                }
            };
        }

    }

    /**
     * Represents a slice of the provided byte buffer array. Two {@link Slice<T>} instances are equal if they both
     * point to the same index in the {@link UnixFSCircularBlockBuffer}, as they essentially point to the same data.
     * This may be a little counterintuitive as two {@link Slice<T>} instances of differing generic types may be the
     * same even if they house different values.
     */
    public class Slice<T> {

        private final int index;

        private final T value;

        private final ByteBuffer buffer;

        private final UnixFSCircularBlockBuffer owner = UnixFSCircularBlockBuffer.this;

        private Slice(final int index, final T value, final ByteBuffer buffer) {
            this.index = index;
            this.value = value;
            this.buffer = buffer;
        }

        /**
         * Gets the index in the circular block buffer of the {@link Slice<T>}.
         *
         * @return the index
         */
        public int getIndex() {
            return index;
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

        /**
         * Flat maps this {@link Slice<T>} by applying the supplied mapper, and returning a new instance of
         *
         * @param mapper the mapper function
         * @param <U> the desired type
         * @return a new {@link Slice<U>} which was derived from the original value
         */
        public <U> Slice<U> map(final Function<T, U> mapper) {
            final U value = mapper.apply(getValue());
            return new Slice<U>(index, value, buffer);
        }

    }

}

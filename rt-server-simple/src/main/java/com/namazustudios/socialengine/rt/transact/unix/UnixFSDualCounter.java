package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.FatalException;

import javax.sound.midi.SysexMessage;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

/**
 * Represents a wraparound counter with a leading and trailing value. This allows multiple threads to safely share an
 * integer-based index of resources independently. When acquiring a resource, a thread will increment the leading value
 * and use that to reference a particular piece of data. When the thread is finished, it will increment the trailing
 * value thereby allowing another thread to access the shared resource.
 *
 * Both values, leading and trailing, automatically wrap around a fixed-size maximum value. If the leading value wraps
 * around and catches up to the trailing value, then all resources are exhausted and the counter will throw an instance
 * of {@link FatalException}. If, while incrementing the trailing value, a thread catches up to the leading value then
 * this counter will throw an instance of {@link IllegalStateException} indicating a situation implying programmer
 * error and likely data corruption.
 *
 * They are backed by a single instance of an {@link AtomicLong} and bit-shifting is used to pack the values together.
 *
 * Because this dual-counter transparently supports wraparound, the trailing value may not necessarily be less than the
 * leading value as the values wrap around automatically.
 */
class UnixFSDualCounter {

    private final int max;

    private final AtomicLong counter;

    /**
     * Creates an instance of {@link UnixFSDualCounter} with the default max value, which is the max value of an
     * integer.
     */
    public UnixFSDualCounter() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a dual count with the supplied maximum value. The max value represents the actual number the counter will
     * hit, including zero. Therefore, if using this to index an array, the max value shoudl be one less than the total
     * size of the array.
     *
     * @param max the maximum value.
     */
    public UnixFSDualCounter(final int max) {
        this(max, new AtomicLong(pack(max, max)));
    }

    /**
     * Same as {@link #UnixFSDualCounter(int)}, but allows for the caller to specify its own AtomicLong, such use case
     * is intended for use
     * @param max
     * @param counter
     */
    public UnixFSDualCounter(final int max, final AtomicLong counter) {
        if (max <= 0) throw new IllegalArgumentException("Maximum value too low: " + max);
        this.max = max;
        this.counter = counter;
    }

    /**
     * Tests if the counter is empty, that is to say that no values are valid at all.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        final long value = counter.get();
        return leading(value) == trailing(value);
    }

    /**
     * Tests if the counter is full, that is to say the next operation involving an increment of the leading value
     * would result in an exception.
     *
     * @return true if the data is full
     */
    public boolean isFull() {
        final long value = counter.get();
        final int trailing = trailing(value);
        final int leading = increment(leading(value));
        return trailing == leading;
    }

    /**
     * Increments the leading value. This may be incremented up to the maximum value until with a call to
     * {@link #incrementAndGetTrailing()} before throwing an instance of {@link FatalException}.
     *
     * @return the post-incremented value
     * @throws FatalException if the count has been exhausted
     */
    public int incrementAndGetLeading() {

        long expected;
        int leading, trailing;

        do {

            expected = counter.get();

            trailing = trailing(expected);
            leading = increment(leading(expected));

            if (trailing == leading) {
                // This should happen when the entire pool is exhausted. This may happen under normal circumstances, but
                // ideally this should be extremely rare. Nonetheless, we protect against data loss by simply throwing
                // an instance of fatal exception to avoid over-writing or corrupting the index. The value is not
                // actually written so this should preserve the data integrity.
                throw new FatalException("Exhausted counter at " + format("trailing==leading==%d", trailing));
            }

        } while (!counter.compareAndSet(expected, pack(trailing, leading)));

        return leading;

    }

    /**
     * Increments the lower value. This may not be incremented past the upper value, if so it indicates programmer error
     * and an instance of {@link IllegalStateException} will be thrown.
     *
     * @return the post-increment value
     */
    public int incrementAndGetTrailing() {

        long expected;
        int leading, trailing;

        do {

            expected = counter.get();

            leading = leading(expected);
            trailing = trailing(expected);

            if (trailing == leading) {
                // This should only happen if the counter was incorrectly used.
                throw new IllegalStateException("Unbalanced trailing " + format("trailing==leading==%d", trailing));
            }

            trailing = increment(trailing);

        } while (!counter.compareAndSet(expected, pack(trailing, leading)));

        return leading;

    }

    /**
     * Gets the snapshot value of this {@link UnixFSDualCounter}. This represents the current state of the counter
     * and is fetched atomically.
     *
     * @return the {@link Snapshot}
     */
    public Snapshot getSnapshot() {
        final long snapshot = counter.get();
        return new Snapshot(max, snapshot);
    }

    @Override
    public String toString() {
        return format("UnixFSDualCounter{%s}", getSnapshot());
    }

    private int increment(final int value) {
        return value == max ? 0 : value + 1;
    }

    private static int leading(final long packed) {
        return (int) (packed >> 32);
    }

    private static int trailing(final long packed) {
        return (int) (packed & 0xFFFFFFFF);
    }

    private static long pack(final int trailing, final int leading) {
        final long lLeading = leading;
        final long lTrailing = trailing;
        return lTrailing | (lLeading << 32);
    }

    /**
     * Gets a snapshot of the counter. This includes both the leading and trailing values.
     */
    public static class Snapshot {

        private final int max;
        private final int leading;
        private final int trailing;
        private final long snapshot;

        private Snapshot(int max, long snapshot) {
            this.max = max;
            this.snapshot = snapshot;
            leading = leading(snapshot);
            trailing = trailing(snapshot);
        }

        /**
         * Gets the snapshot value.
         *
         * @return the snapshot value
         */
        public long getSnapshot() {
            return snapshot;
        }

        /**
         * Gets the leading value.
         *
         * @return the leading value.
         */
        public int getLeading() {
            return leading;
        }

        /**
         * Gets the trailing value.
         *
         * @return the trailing value
         */
        public int getTrailing() {
            return trailing;
        }

        /**
         * A null snapshot was taken when leading == trailing.
         *
         * @return true if empty
         */
        public boolean isNull() {
            return getLeading() == getTrailing();
        }

        @Override
        public String toString() {
            return format("snapshot-%016X (t%d l%d)", snapshot, trailing, leading);
        }

        /**
         * Compares two {@link Snapshot}s using the supplied {@link Snapshot} instance as an absolute reference.
         *
         * @param reference
         * @param other
         * @return
         */
        public int compareTo(final Snapshot reference, final Snapshot other) {

            if (max != reference.max || max != other.max) {
                final String msg = format("All snapshots must have identical max values %d != %d != %d",
                                           max, other.max, reference.max);
                throw new IllegalArgumentException(msg);
            } else if (reference.isNull() || isNull() || other.isNull()) {
                final String msg = format("Cannot compare two snapshots where one or more is null %s %s %s",
                                           this, other, reference);
                throw new IllegalArgumentException(msg);
            }

            final long lThisValue = normalize(reference.trailing);
            final long lOtherValue = normalize(reference.trailing);

            return lThisValue < lOtherValue ? -1 :
                   lThisValue > lOtherValue ?  1 : 0;

        }

        private long normalize(final long reference) {

        }

    }

}

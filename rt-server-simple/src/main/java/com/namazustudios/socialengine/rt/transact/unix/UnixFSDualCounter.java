package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.FatalException;

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

    public UnixFSDualCounter() {
        this(Integer.MAX_VALUE);
    }

    public UnixFSDualCounter(final int max) {
        this(max, new AtomicLong(pack(max, 0)));
    }

    public UnixFSDualCounter(final int max, final AtomicLong counter) {
        this.max = max;
        this.counter = counter;
    }

    /**
     * Increments the leading value. This may be incremented up to the maximum value until with a call to
     * {@link #incrementAndGetTrailing()} before throwing an instance of {@link FatalException}.
     *
     * @return the post-incremented value
     * @throws FatalException if the count has been exhausted
     */
    public int incrementAhdGetLeading() {

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

        } while (counter.compareAndSet(expected, pack(trailing, leading)));

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

            trailing = trailing(expected);
            leading = increment(leading(expected));

            if (trailing == leading) {
                // This should only happen if the counter was incorrectly used.
                throw new IllegalStateException("Unbalanced trailing " + format("trailing==leading==%d", trailing));
            }

        } while (counter.compareAndSet(expected, pack(trailing, leading)));

        return leading;

    }

    public Snapshot getSnapshot() {
        final long snapshot = counter.get();
        return new Snapshot(snapshot);
    }

    private int increment(final int value) {
        return value == max ? 0 : value + 1;
    }

    private static int leading(final long packed) {
        return (int) packed >> 32;
    }

    private static int trailing(final long packed) {
        return (int) packed & 0xFFFFFFFF;
    }

    private static long pack(final int trailing, final int leading) {
        final long lLeading = leading;
        final long lTrailing = trailing;
        return lTrailing | (lLeading << 32);
    }

    /**
     * Gets a snapshot of the counter. This includes both the leading and trailing values.
     */
    public static class Snapshot implements Comparable<Snapshot> {

        private final int max;
        private final long snapshot;

        private Snapshot(int max, long snapshot) {
            this.max = max;
            this.snapshot = snapshot;
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
            return leading(snapshot);
        }

        /**
         * Gets the trailing value.
         *
         * @return the trailing value
         */
        public int getTrailing() {
            return trailing(snapshot);
        }

        @Override
        public String toString() {
            return format("snapshot-%d", getSnapshot());
        }

        @Override
        public int compareTo(final Snapshot snapshot) {
            final int relativeMinimum = Integer.min(getTrailing(), snapshot.getTrailing());

            return 0;
        }

    }

}

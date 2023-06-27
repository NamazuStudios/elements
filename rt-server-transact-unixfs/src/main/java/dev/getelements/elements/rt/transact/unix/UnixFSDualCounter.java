package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.transact.FatalException;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.IntStream.concat;
import static java.util.stream.IntStream.rangeClosed;

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
 * error and likely data corruption.UnixFSTransactionProgramBuilder
 *
 * They are backed by a single instance of an {@link AtomicLong} and bit-shifting is used to pack the values together.
 *
 * Because this dual-counter transparently supports wraparound, the trailing value may not necessarily be less than the
 * leading value as the values wrap around automatically.
 */
public class UnixFSDualCounter {

    public static final long EMPTY_MASK_LONG = 0x8000000080000000l;

    private final int max;

    private final UnixFSAtomicLong counter;

    /**
     * Creates a dual count with the supplied maximum value. The max value represents the actual number the counter will
     * hit, including zero. Therefore, if using this to index an array, the max value shoudl be one less than the total
     * size of the array.
     *
     * @param max the maximum value, inclusive
     */
    public UnixFSDualCounter(final int max) {
        this(max, new AtomicLong(pack(-1, -1)));
    }

    /**
     * Same as {@link #UnixFSDualCounter(int)}, but allows for the caller to specify its own AtomicLong used.
     *
     * @param max
     * @param counter
     */
    public UnixFSDualCounter(final int max, final AtomicLong counter) {
        this(max, UnixFSAtomicLong.wrap(counter));
    }

    /**
     * Allows for the caller to specify an arbitrary {@link UnixFSAtomicLong}.
     *
     * @param max the max value
     * @param counter the counter
     */
    public UnixFSDualCounter(final int max, final UnixFSAtomicLong counter) {
        if (max <= 0) throw new IllegalArgumentException("Maximum value too low: " + max);
        this.max = max;
        this.counter = counter;
    }

    /**
     * Resets this counter ensuring that both leading/trailing values are equal to zero and the sign bits are set
     * indicating that the counter is empty.
     */
    public UnixFSDualCounter reset() {
        counter.set(EMPTY_MASK_LONG);
        return this;
    }

    /**
     * Tests if the counter is empty, that is to say that no values are valid at all.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        final long value = counter.get();
        return isEmpty(value);
    }

    /**
     * Tests if the counter is full, that is to say the next operation involving an increment of the leading value
     * would result in an exception.
     *
     * @return true if the data is full
     */
    public boolean isFull() {
        final long value = counter.get();
        return isFull(value, max);
    }

    /**
     * Returns the distance between the trailing and leading value.
     * @return
     */
    public int size() {
        final long value = counter.get();
        return size(value, max);
    }

    /**
     * Increments the leading value. This may be incremented up to the maximum value until with a call to
     * {@link #getTrailingAndIncrement()} before throwing an instance of {@link FatalException}.
     *
     * @return the pre-incremented value
     * @throws FatalException if the count has been exhausted
     */
    public int incrementLeadingAndGet() {

        long expected, update;
        int leading, trailing;

        do {

            expected = counter.get();
            leading = leading(expected);
            trailing = trailing(expected);

            if (isEmpty(expected)) {
                update = pack(trailing, leading) & ~EMPTY_MASK_LONG;
            } else {
                leading = checkAndIncrementLeading(leading, trailing);
                update = pack(trailing, leading);
            }

        } while (!counter.compareAndSet(expected, update));

        return leading(update);

    }

    /**
     * Increments the leading value. This may be incremented up to the maximum value until with a call to
     * {@link #getTrailingAndIncrement()} before throwing an instance of {@link FatalException}.
     *
     * @return the post-incremented value
     * @throws FatalException if the count has been exhausted
     * @return a {@link Snapshot} of the state of this {@link UnixFSDualCounter}
     */
    public Snapshot incrementLeadingAndGetSnapshot() {

        long packed;
        long expected;
        int leading, trailing;

        do {

            expected = counter.get();
            leading = leading(expected);
            trailing = trailing(expected);

            if (isEmpty(expected)) {
                packed = pack(trailing, leading) & ~EMPTY_MASK_LONG;
            } else {
                leading = checkAndIncrementLeading(leading, trailing);
                packed = pack(trailing, leading);
            }

        } while (!counter.compareAndSet(expected, packed));

        return new Snapshot(max, packed);

    }

    private int checkAndIncrementLeading(final int leading, final int trailing) {

        checkForValidity(leading, trailing);

        final int nextLeading = increment(leading);

        if (nextLeading == trailing) {
            // This should happen when the entire pool is exhausted. This may happen under normal circumstances, but
            // ideally this should be extremely rare. Nonetheless, we protect against data loss by simply throwing
            // an instance of fatal exception to avoid over-writing or corrupting the index. The value is not
            // actually written so this should preserve the data integrity.
            throw new FatalException("Exhausted counter at " + format("trailing==leading==%d", trailing));
        }

        return nextLeading;

    }

    /**
     * Increments the trailing value. This may not be incremented past the upper value, if so it indicates programmer
     * error and an instance of {@link IllegalStateException} will be thrown.
     *
     * The counter will be flagged as empty once the trailing value matches the leading value.
     *
     * @return the pre-increment value
     */
    public int getTrailingAndIncrement() {

        long expected, update;
        int leading, trailing, nextTrailing;

        do {

            expected = counter.get();

            if (isEmpty(expected)) {
                // This should only happen if the counter was incorrectly used. However, this doesn't constitute a
                // situation in which the counter was exhausted, this is simply improper use of the counter.
                throw new IllegalStateException("Unbalanced trailing. Dual counter is empty.");
            }

            leading = leading(expected);
            trailing = trailing(expected);
            nextTrailing = increment(trailing);

            if (trailing == leading) {
                // We have brought the trailing value around to the beginning. This means that we should simply clear
                // out the value by negating the sign bit indicating that both numbers should be
                update = pack(trailing, leading) | EMPTY_MASK_LONG;
            } else {
                // In this case we did not hit the leading value. Threfore, we just pack the numbers together so wecan
                // attempt the update operation.
                update = pack(nextTrailing, leading);
            }

        } while (!counter.compareAndSet(expected, update));

        return trailing;

    }

    private void checkForValidity(final int leading, final int trailing) {
        if (leading > max  || trailing > max || leading < -max || trailing < -max) {
            // This should not happen unless the actual counter was corrupted. For example, the atomic long was
            // set with invalid values from the beginning or the underlying file backign the counter has been corrupted.
            // In the case where we detect this condition, we
            final String values = format("leading=%d trailing=%d (max=%d)", leading, trailing, max);
            throw new FatalException("Counter has invalid values: " + values);
        }
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

    /**
     * Given a {@link Snapshot}, this attempts to increment the trailing value. If the increment succeeds, this method
     * returns true indicating so. Otherwise, it returns false. This allows for external code to safely and
     * deterministically replenish trailing values managed by this counter.
     *
     * @param snapshot the {@link Snapshot
     * @return true, if successful. false otherwise.
     */
    public boolean compareAndIncrementTrailing(final Snapshot snapshot) {

        long update;

        int nextTrailing = increment(snapshot.getTrailing());

        if (snapshot.getTrailing() == snapshot.getLeading()) {
            // We have brought the trailing value around to the beginning. This means that we should simply clear
            // out the value by negating the sign bit indicating that both numbers should be
            update = pack(snapshot.getTrailing(), snapshot.getTrailing()) | EMPTY_MASK_LONG;
        } else {
            // In this case we did not hit the leading value. Therefore, we just pack the numbers together so we can
            // attempt the update operation.
            update = pack(nextTrailing, snapshot.getLeading());
        }

        return counter.compareAndSet(snapshot.getSnapshot(), update);

    }

    @Override
    public String toString() {
        return format("UnixFSDualCounter{%s}", getSnapshot());
    }

    private int increment(final int value) {
        return value == max ? 0 : value + 1;
    }

    /**
     * Gets the leading value, ignoring the empty state.
     *
     * @return the leading
     */
    public int getLeading() {
        final long value = counter.get();
        return leading(value & ~EMPTY_MASK_LONG);
    }

    /**
     * Gets the trailing value, ignoring the empty state.
     *
     * @return the trailing value
     */
    public int getTrailing() {
        final long value = counter.get();
        return trailing(value & ~EMPTY_MASK_LONG);
    }

    /**
     * Tests if the packed value represents a full counter. That is the sign bits of the upper and lower values are
     * set to 0 and the upper and lower bits are equal.
     *
     * @param packed the packed value
     * @return true if empty, false otherwise
     */
    public static boolean isFull(final long packed, final int max) {
        final int trailing = trailing(packed);
        final int leading = leading(packed);
        final int next = increment(leading, max);
        return (packed & EMPTY_MASK_LONG) != EMPTY_MASK_LONG && trailing == next;
    }

    /**
     * Given the 64-bit packed value, this method reads both the leading and trailing values and finds the distance
     * between the two numbers.
     *
     * @param packed the packed value
     * @param max the max value
     * @return the distance between the two numbers
     */
    public static int size(final long packed, final int max) {

        if (isEmpty(packed)) return 0;

        final int leading = leading(packed);
        final int trailing = trailing(packed);

        if (trailing < leading) {
            return 1 + leading - trailing;
        } else if (trailing > leading) {
            return  1 + (max - trailing) + leading;
        } else {
            return 1;
        }

    }

    /**
     * Tests if the packed value represents an empty counter. That is the sign bits of the upper and lower values are
     * set to 1.
     *
     * @param packed the packed value
     * @return true if empty, false otherwise
     */
    public static boolean isEmpty(final long packed) {
        return (packed & EMPTY_MASK_LONG) == EMPTY_MASK_LONG;
    }

    /**
     * Gets the leading value from the the packed long.
     *
     * @param packed the packed long
     * @return the leading value
     */
    public static int leading(final long packed) {
        return (int) (packed >>> 32);
    }

    /**
     * Sets the leading value from the packed long.
     *
     * @param packed the packed long
     * @return the trailing value
     */
    public static int trailing(final long packed) {
        return (int) (packed & 0xFFFFFFFF);
    }

    /**
     * Increments the supplied value wrapping up to the max value.
     *
     * @param value the value to increment
     * @param max the max value
     * @return the incremented value
     */
    public static int increment(final int value, final int max) {
        return value == max ? 0 : value + 1;
    }

    /**
     * Packs the leading and trailing values together and returns the long representation.
     *
     * @param trailing the trailing value
     * @param leading the leading value
     *
     * @return the packed value
     */
    public static long pack(final int trailing, final int leading) {
        final long lLeading = ((long)leading << 32);
        final long lTrailing = ((long)trailing << 32) >>> 32;
        final long packed = lTrailing | lLeading;
        return packed;
    }

    /**
     * Gets the maximum value from this counter.
     *
     * @return the max value
     */
    public int getMax() {
        return max;
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
            if (leading > max) throw new IllegalArgumentException("leading exceeds max value.");
            if (trailing > max) throw new IllegalArgumentException("trailing exceeds max value.");
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
        public boolean isEmpty() {
            return UnixFSDualCounter.isEmpty(snapshot);
        }

        /**
         * Gets the max value.
         *
         * @return the max value
         */
        public int getMax() {
            return max;
        }

        /**
         * Returns {@link IntStream} representing the range of valid integers covered by this stream.
         *
         * @return this range.
         */
        public IntStream range() {
            if (isEmpty()) {
                return IntStream.empty();
            } else if (trailing < leading) {
                return rangeClosed(trailing, leading);
            } else if (trailing > leading) {
                final IntStream trailingToEnd = rangeClosed(trailing, max);
                final IntStream beginToLeading = rangeClosed(0, leading);
                return concat(trailingToEnd, beginToLeading);
            } else {
                return IntStream.of(leading);
            }
        }

        /**
         * Returns {@link IntStream} representing the range of valid integers covered by this stream, in reverse order.
         *
         * @return this range.
         */
        public IntStream reverseRange() {
            if (isEmpty()) {
                return IntStream.empty();
            } else if (trailing < leading) {
                return reverseRangeClosed(trailing, leading);
            } else if (trailing > leading) {
                final IntStream leadingToBegin = reverseRangeClosed(leading, 0);
                final IntStream maxToTrailing = reverseRangeClosed(max, trailing);
                return concat(leadingToBegin, maxToTrailing);
            } else {
                return IntStream.of(leading);
            }
        }

        /**
         * Checks if the supplied index is in range.
         *
         * @param index the index
         * @return true if in range, false otherwise
         */
        public boolean inRange(final int index) {
            if (isEmpty() || index < 0) {
                return false;
            } else if (trailing < leading) {
                return index >= trailing && index <= leading;
            } else if (trailing > leading) {
                return index <= trailing && index >= leading;
            } else {
                return index == leading;
            }
        }

        private static IntStream reverseRangeClosed(final int from, final int to) {
            return rangeClosed(from, to).map(i -> to - i + from);
        }

        /**
         * Constructs a {@link Snapshot} from the integral values.
         *
         * @param max the max value
         * @param snapshot the long value
         * @return the newly created {@link Snapshot}
         */
        static Snapshot fromIntegralValues(final int max, final long snapshot) {
            return new Snapshot(max, snapshot);
        }

        @Override
        public String toString() {

            final boolean empty = isEmpty();

            return format(
                empty ? "snapshot-%016X (t-0x%X l-0x%X) empty %b" : "snapshot-%016X (t-%d l-%d) empty-%b full-%b",
                snapshot,
                empty ? -trailing : trailing,
                empty ? -leading : leading,
                empty,
                isFull(snapshot, max)

            );

        }

    }

}

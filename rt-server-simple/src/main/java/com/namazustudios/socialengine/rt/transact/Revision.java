package com.namazustudios.socialengine.rt.transact;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BinaryOperator;

import static java.util.Optional.empty;

public interface Revision<ValueT> extends Comparable<Revision<?>> {

    /**
     * Represents the zero-revision. This is a special {@link Revision} that is older than every other revision, except
     * itself, to which it is equal.
     */
    Revision<Void> ZERO = new Revision<Void>() {

        @Override
        public String getUniqueIdentifier() {
            return "zero";
        }

        @Override
        public int compareTo(Revision<?> o) {
            return this == o ? 0 : -1;
        }

        @Override
        public <T> Revision<T> comparableTo() {
            return (Revision<T>)this;
        }

    };

    /**
     * Represents a revision in the far-flung future. This is a special {@link Revision} that is newer than every other
     * revision, except itself, to which it is equal.
     */
    Revision<Void> INFINITY = new Revision<Void>() {

        @Override
        public String getUniqueIdentifier() {
            return "infinity";
        }

        @Override
        public int compareTo(Revision<?> o) {
            return this == o ? 0 : 1;
        }

        @Override
        public <T> Revision<T> comparableTo() {
            return (Revision<T>) this;
        }

    };

    /**
     * Represents the latest revision. This is a special {@link Revision} that is newer than almost every other
     * revision.  It is is equal to itself and less than {@link #infinity()}.
     */
    Revision<Void> LATEST = new Revision<Void>() {

        @Override
        public String getUniqueIdentifier() {
            return "latest";
        }

        @Override
        public int compareTo(Revision<?> o) {
            return this == o ? 0 : INFINITY == o ? -1 : 1;
        }

        @Override
        public <T> Revision<T> comparableTo() {
            return (Revision<T>) this;
        }

    };

    /**
     * A special type of Revision that is before all other {@link Revision} instances.
     *
     * @return the zero revision
     */
    static <U> Revision<U> zero() {
        return ZERO.comparableTo();
    }

    /**
     * A special type of Revision that is after all other {@link Revision} instances.
     *
     * @return the infinity revision
     */
    static <U> Revision<U> infinity() {
        return INFINITY.comparableTo();
    }

    /**
     * Represents the latest revision of any particular resource.  Similar to {@link #infinity()} but can be used to
     * represent a specific tangible instance.
     *
     * @param <U>
     * @return the latest revision
     */
    static <U> Revision<U> latest() { return LATEST.comparableTo(); }

    /**
     * Returns a string uniquely identifying this {@link Revision}.  This may be any value, except for the literal value
     * "&ltzero&gt"
     *
     * @return the unique ID
     */
    String getUniqueIdentifier();

    /**
     * Returns the value at this particular revision.  The supplied value may not exist, hence this returns an instance
     * of {@link Optional<ValueT>}.
     *
     * @return the optional value
     */
    default Optional<ValueT> getValue() {
        return empty();
    }

    /**
     * Returns true if this {@link Revision<?>} is after the supplied {@link Revision<?>}.
     *
     * @param revision the other revision to check
     * @return true if this is after, false otherwise
     *
     */
    default boolean isAfter(final Revision<?> revision) {
        return compareTo(revision) > 0;
    }

    /**
     * Returns true if this {@link Revision} is before the supplied {@link Revision<?>}
     *
     * @param revision the revision to check
     * @return true if this is before, false otherwise
     */
    default boolean isBefore(final Revision<?> revision) {
        return compareTo(revision) < 0;
    }

    /**
     * Returns true if this {@link Revision<?>} is before or the same as the supplied {@link Revision<?>}
     *
     * @param revision to check
     * @return true if before or same, false otherwise
     */
    default boolean isBeforeOrSame(final Revision<?> revision) {
        return compareTo(revision) <= 0;
    }

    /**
     * Returns a new {@link Revision} that is comparable to the requested type.
     *
     * @param <T> the requested type
     * @return a {@link Revision<T>} that will compare to this one.  No data is availble.
     */
    default <T> Revision<T> comparableTo() {
        return new Revision<T>() {

            @Override
            public int compareTo(final Revision<?> o) {
                return Revision.this.compareTo(o);
            }

            @Override
            public String getUniqueIdentifier() {
                return Revision.this.getUniqueIdentifier();
            }

            @Override
            public <U> Revision<U> comparableTo() {
                return Revision.this.comparableTo();
            }

        };
    }

}

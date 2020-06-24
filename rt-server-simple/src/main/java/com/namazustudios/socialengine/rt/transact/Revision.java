package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.util.LazyValue;
import sun.security.krb5.internal.APRep;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

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
     *placehodler
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
     * A convenience wrapper around the {@link Optional<ValueT>} returned by this instance's {@link #getValue()}
     * method.  The supplied {@link Function} will map the value according to the rules spelled out by
     * {@link Optional#map(Function)}
     *
     * @param mapper
     * @param <U>
     * @return
     */
    default <U> Revision<U> map(final Function<ValueT, U> mapper) {
        return new Revision<U>() {
            @Override
            public int compareTo(Revision<?> o) {  return Revision.this.compareTo(o); }
            @Override
            public String getUniqueIdentifier() { return Revision.this.getUniqueIdentifier(); }
            @Override
            public Optional<U> getValue() { return Revision.this.getValue().map(mapper); }
        };
    }

    /**
     * Returns a new {@link Revision<U>} with the supplied value.  Useful if the current revision does not have a value
     * and you are willing to supply the value.
     *
     * @param value the value to set
     * @param <U> the type of the value
     *
     * @return a new {@link Revision} with a new value
     */
    default <U> Revision<U> withValue(final U value) {
        return withOptionalValue(Optional.of(value));
    }

    /**
     * Returns a new {@link Revision<U>} with the supplied {@link Optional<U>} value.
     *
     * @param optionalValue an {@link Optional<U>} representing the value.
     * @param <U> the
     * @return
     */
    default <U> Revision<U> withOptionalValue(final Optional<U> optionalValue) {
        return new Revision<U>() {
            @Override
            public int compareTo(Revision<?> o) { return Revision.this.compareTo(o); }
            @Override
            public String getUniqueIdentifier() { return Revision.this.getUniqueIdentifier(); }
            @Override
            public Optional<U> getValue() { return optionalValue; }
        };
    }

    /**
     * Filters this {@link Revision<ValueT>} such that it will report an empty value if the supplied
     * {@link Predicate<ValueT>} does not hold true.
     *
     * @param predicate the {@link Predicate<ValueT>} to test
     * @return a newly filtered {@link Revision<ValueT>}
     */
    default Revision<ValueT> filter(final Predicate<ValueT> predicate) {
        return withOptionalValue(getValue().filter(predicate));
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
            public int compareTo(final Revision<?> o) { return Revision.this.compareTo(o); }
            @Override
            public String getUniqueIdentifier() { return Revision.this.getUniqueIdentifier(); }
            @Override
            public <U> Revision<U> comparableTo() { return Revision.this.comparableTo(); }
        };
    }

    interface Factory {

        /**
         * Creates a {@link Revision<T>} with the supplied revision ID and value.  This parses the revision ID, as
         * specified by the {@link Revision#getUniqueIdentifier()}.  The resulting {@link Revision<T>} will have
         * no value, initially.
         *
         * @param at a string indicating the unique revision ID.
         * @param <T>
         * @return a new {@link Revision<T>} instance
         */
        <T> Revision<T> create(String at);

    }

}

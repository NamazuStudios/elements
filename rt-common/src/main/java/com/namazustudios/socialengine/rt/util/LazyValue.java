package com.namazustudios.socialengine.rt.util;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A value that lazily loads the first time it is fetched. This is not thread safe.
 *
 * @param <T>
 */
public class LazyValue<T> implements Supplier<T> {

    private static final Object UNASSIGNED = new Object() {
        @Override
        public String toString() {
            return "UNASSIGNED";
        }
    };

    T t = (T)UNASSIGNED;

    private final Supplier<T> tSupplier;

    public LazyValue(final Supplier<T> tSupplier) {
        this.tSupplier = tSupplier;
    }

    /**
     * Gets the value of this {@link LazyValue<T>}, computing it if it was not already computed.
     *
     * @return the optional value
     */
    public T get() {

        T t = this.t;

        if (t == UNASSIGNED) {
            t = this.t = tSupplier.get();
        }

        return t;

    }

    /**
     * Gets an {@link Optional} representing this {@link LazyValue<T>}
     * @return
     */
    public Optional<T> getOptional() {
        return  t == UNASSIGNED ? Optional.empty() : Optional.of(t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LazyValue)) return false;

        LazyValue<?> lazyValue = (LazyValue<?>) o;

        if (t != null ? !t.equals(lazyValue.t) : lazyValue.t != null) return false;
        return tSupplier != null ? tSupplier.equals(lazyValue.tSupplier) : lazyValue.tSupplier == null;
    }

    @Override
    public int hashCode() {
        int result = t != null ? t.hashCode() : 0;
        result = 31 * result + (tSupplier != null ? tSupplier.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LazyValue{" + t + "}";
    }

}

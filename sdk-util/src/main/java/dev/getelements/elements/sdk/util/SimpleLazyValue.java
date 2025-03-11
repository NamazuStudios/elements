package dev.getelements.elements.sdk.util;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A value that lazily loads the first time it is fetched. This is not thread safe.
 *
 * @param <T>
 */
public class SimpleLazyValue<T> implements LazyValue<T> {

    static final Object UNASSIGNED = new Object() {
        @Override
        public String toString() {
            return "UNASSIGNED";
        }
    };

    T t = (T)UNASSIGNED;

    private Supplier<T> tSupplier;

    public SimpleLazyValue(final Supplier<T> tSupplier) {
        requireNonNull(tSupplier);
        this.tSupplier = tSupplier;
    }

    @Override
    public T get() {

        T t = this.t;

        if (t == UNASSIGNED) {
            t = this.t = tSupplier.get();
            tSupplier = null;
        }

        return t;

    }

    @Override
    public Optional<T> getOptional() {
        return  t == UNASSIGNED ? Optional.empty() : Optional.of(t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleLazyValue)) return false;

        SimpleLazyValue<?> lazyValue = (SimpleLazyValue<?>) o;

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

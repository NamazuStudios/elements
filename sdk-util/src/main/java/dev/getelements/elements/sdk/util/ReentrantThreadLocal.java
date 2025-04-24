package dev.getelements.elements.sdk.util;

import java.util.Optional;

/**
 * Backed by a {@link ThreadLocal<T>}, this allows for the creation of a thread-local scope which can be entered and
 * exited multiple times, provided that the underlying calls are balanced.
 *
 * @param <T>
 */
public class ReentrantThreadLocal<T> {

    private final ThreadLocal<StackScope> current;

    /**
     * Creates a new {@link ReentrantThreadLocal<T>}
     */
    public ReentrantThreadLocal() {
        current = new ThreadLocal<>();
    }

    /**
     * Gets the current value throwing an instance of {@link IllegalStateException} if there is not a current value.
     * @return the current value.
     */
    public T getCurrent() {
        return getCurrentOptional().orElseThrow(() -> new IllegalStateException("Not in scope."));
    }

    /**
     * Returns the current value as an {@link Optional<T>}.
     *
     * @return the current value as an {@link Optional<T>}.
     */
    public Optional<T> getCurrentOptional() {
        final Scope<T> scope = current.get();
        return Optional.ofNullable(scope).map(Scope::get);
    }

    /**
     * Ensures that the current value is empty, throwing an instance of {@link IllegalStateException} if the
     * object is not in scope.
     */
    public void ensureEmpty() {
        if (current.get() != null) throw new IllegalStateException("Currently in scope. Expecting empty scope.");
    }

    /**
     * Enters scope with the supplied value of T.
     *
     * @param t the value of t
     * @return a {@link Scope} which will hold the value until it is closed, reverting it to the previous value.
     */
    public Scope<T> enter(final T t) {

        final var existing = current.get();

        if (existing == null) {
            final StackScope context = new StackScope(t);
            current.set(context);
            return context;
        } else {
            return existing.push(t);
        }

    }

    /**
     * The current scope for the {@link ReentrantThreadLocal}
     *
     * @param <U> the type contained therein
     */
    public interface Scope<U> extends AutoCloseable {

        U get();

        @Override
        void close();

    }

    private class StackScope implements Scope<T> {

        private final T t;

        private StackScope next;

        private StackScope prev;

        public StackScope(final T t) {
            this.t = t;
        }

        @Override
        public T get() {
            return t;
        }

        public StackScope push(final T t) {
            final StackScope context = new StackScope(t);
            context.prev = this;
            current.set(next = context);
            return context;
        }

        @Override
        public void close() {
            if (next != null) next.prev = prev;
            if (prev != null) prev.next = next;
            if (current.get() == this) current.set(prev);
            if (next == null && prev == null) current.set(null);
        }

    }

}

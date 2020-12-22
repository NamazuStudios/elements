package com.namazustudios.socialengine.rt;

import java.util.Optional;

/**
 * Backed by a {@link ThreadLocal<T>}, this allows for the creation of a thread-local scope which can be entered and
 * exited multiple times, provided that the underlying calls are balanced.
 *
 * @param <T>
 */
public class ReentrantThreadLocal<T> {

    private final ThreadLocal<StackContext> current;

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
        final Context<T> context = current.get();
        return Optional.ofNullable(context).map(Context::get);
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
     * @return a {@link Context<T>} which will hold the value until it is closed, reverting it to the previous value.
     */
    public Context<T> enter(final T t) {

        final var existing = current.get();

        if (existing == null) {
            final StackContext context = new StackContext(t);
            current.set(context);
            return context;
        } else {
            return existing.push(t);
        }

    }

    public interface Context<U> extends AutoCloseable {

        U get();

        @Override
        void close();

    }

    private class StackContext implements Context<T> {

        private final T t;

        private StackContext next;

        private StackContext prev;

        public StackContext(final T t) {
            this.t = t;
        }

        @Override
        public T get() {
            return t;
        }

        public StackContext push(final T t) {
            final StackContext context = new StackContext(t);
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

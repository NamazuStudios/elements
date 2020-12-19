package com.namazustudios.socialengine.rt;

/**
 * Backed by a {@link ThreadLocal<T>}, this allows for the creation of a thread-local scope which can be entered and
 * exited multiple times, provided that the underlying calls are balanced.
 *
 * @param <T>
 */
public class ReentrantThreadLocal<T> {

    private final ThreadLocal<StackContext> current;

    public ReentrantThreadLocal() {
        current = new ThreadLocal<>();
    }

    public T getCurrent() {
        final Context<T> context = current.get();
        if (context == null) throw new IllegalStateException("Not in scope.");
        return context.get();
    }

    public void ensureEmpty() {
        if (current.get() != null) throw new IllegalStateException("Currently in scope. Expecting empty scope.");
    }

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

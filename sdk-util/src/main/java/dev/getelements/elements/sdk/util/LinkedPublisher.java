package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.System.identityHashCode;

/**
 * A non-threadsafe publisher which tracks subscribers in a linked list. For the purposes of debugging this implements
 * {@link Iterable} which most IDEs will recognize and render as sequence in the debugger.
 *
 * @param <T> the type of event to publish
 */
public class LinkedPublisher<T> extends AbstractPublisher<T> implements Iterable<Object> {

    private final LinkedConsumer<T> first = new LinkedConsumer<T>() {
        @Override
        public String toString() {
            return "LinkedListConsumer Anchor@"+ identityHashCode(this);
        }
    };

    private LinkedConsumer<T> last = first;

    public LinkedPublisher() {
        this(LoggerFactory.getLogger(LinkedPublisher.class));
    }

    public LinkedPublisher(final Logger logger) {
        super(logger);
    }

    @Override
    public Subscription subscribe(final BiConsumer<Subscription, ? super T> consumer) {
        final var current = last = last.andThenTry(consumer);
        return current::remove;
    }

    @Override
    public void clear() {
        first.clear();
        last = first;
    }

    @Override
    public void publish(final T t, final Consumer<? super T> onFinish, final Consumer<Throwable> onException) {

        first.tryAcceptAll(t, onException);

        try {
            onFinish.accept(t);
        } catch (Exception ex) {
            handleException(onException, ex);
        }

    }

    /**
     * Used only to facilitate debugging. The returned Objects are internal implementation details and not intended
     * to be used to execute business logic of the application.
     *
     * @return an {@link Iterator} over the subscribers.
     */
    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {

            LinkedConsumer<?> current = first.next;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Object next() {
                final var result = current;
                current = current.next;
                return result.delegate;
            }

        };
    }

    private class LinkedConsumer<ConsumedT> {

        private final BiConsumer<Subscription, ? super ConsumedT> delegate;

        private LinkedConsumer<ConsumedT> prev = this;

        private LinkedConsumer<ConsumedT> next = null;

        private final Subscription subscription = () -> remove();

        public LinkedConsumer() {
            this.delegate = (s, t) -> {};
        }

        public LinkedConsumer(final BiConsumer<Subscription, ? super ConsumedT> delegate) {
            this.delegate = delegate;
        }

        void tryAcceptAll(
                final ConsumedT t,
                final Consumer<Throwable> onException) {

            LinkedConsumer<ConsumedT> current = this;

            do {
                final LinkedConsumer<ConsumedT> next = current.next;
                current.tryAccept(t, onException);
                current = next;
            } while(current != null);

        }

        public void tryAccept(final ConsumedT t, final Consumer<Throwable> onException) {
            try {
                delegate.accept(subscription, t);
            } catch (Exception ex) {
                handleException(onException, ex);
            }
        }

        public LinkedConsumer<ConsumedT> andThenTry(final BiConsumer<Subscription, ? super ConsumedT> next) {
            return andThenTry(new LinkedConsumer<>(next));
        }

        public LinkedConsumer<ConsumedT> andThenTry(final LinkedConsumer<ConsumedT> next) {
            if (this.next != null)
                throw new IllegalStateException();
            this.next = next;
            next.prev = this;
            return next;
        }

        public void remove() {
            if (prev != null) prev.next = next;
            if (next != null) next.prev = prev;
            if (this == last) last = (LinkedConsumer<T>) prev; // Cast.  Damnit.  Cast!
            next = null;
            prev = null;
        }

        public void clear() {
            next = null;
            prev = null;
        }

    }

}

package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class SimplePublisher<T> implements AsyncPublisher<T> {

    private static final Logger logger = LoggerFactory.getLogger(SimplePublisher.class);

    private final LinkedConsumer<T> first = new LinkedConsumer<>();

    private LinkedConsumer<T> last = first;

    @Override
    public Subscription subscribe(final Consumer<T> consumer) {
        final LinkedConsumer<T> current = last = last.andThenTry(consumer);
        return () -> current.remove();
    }

    @Override
    public void publish(final T t) {
        first.tryAcceptAll(t);
    }

    @Override
    public void publishAsync(final T t) {
        first.tryAcceptAll(t);
    }

    @Override
    public void publish(final T t, final Consumer<T> onFinish) {

        first.tryAcceptAll(t);

        try {
            onFinish.accept(t);
        } catch (Exception ex) {
            logger.error("Unexpected exception dispatching consumer {}.", onFinish, ex);
        }

    }

    @Override
    public void publishAsync(T t, Consumer<T> onFinish) {

        first.tryAcceptAll(t);

        try {
            onFinish.accept(t);
        } catch (Exception ex) {
            logger.error("Unexpected exception dispatching consumer {}.", onFinish, ex);
        }

    }

    @Override
    public void clear() {
        first.clear();
        last = first;
    }

    private class LinkedConsumer<ConsumedT> {

        private final Consumer<ConsumedT> delegate;

        private LinkedConsumer<ConsumedT> prev = this;

        private LinkedConsumer<ConsumedT> next = null;

        public LinkedConsumer() {
            this.delegate = t -> {};
        }

        public LinkedConsumer(final Consumer<ConsumedT> delegate) {
            this.delegate = delegate;
        }

        public void tryAcceptAll(final ConsumedT t) {

            LinkedConsumer<ConsumedT> current = this;

            do {
                final LinkedConsumer<ConsumedT> next = current.next;
                current.tryAccept(t);
                current = next;
            } while(current != null);

        }

        public void tryAccept(ConsumedT t) {
            try {
                delegate.accept(t);
            } catch (Exception ex) {
                logger.error("Unexpected exception dispatching consumer.", ex);
            }
        }

        public LinkedConsumer<ConsumedT> andThenTry(final Consumer<ConsumedT> next) {
            return andThenTry(new LinkedConsumer<>(next));
        }

        public LinkedConsumer<ConsumedT> andThenTry(final LinkedConsumer<ConsumedT> next) {
            if (next != null) throw new IllegalStateException();
            this.next = next;
            next.prev = this;
            return next;
        }

        public void remove() {
            prev.next = next;
            next.prev = prev;
        }

        public void clear() {
            next = null;
            prev = null;
        }

    }

}

package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimplePublisher<T> implements AsyncPublisher<T> {

    private static final Logger logger = LoggerFactory.getLogger(SimplePublisher.class);

    private final LinkedConsumer<T> first = new LinkedConsumer<>();

    private LinkedConsumer<T> last = first;

    @Override
    public <U extends T> Subscription subscribe(final BiConsumer<Subscription, ? super U> consumer) {
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

        private final BiConsumer<Subscription, ? super ConsumedT> delegate;

        private LinkedConsumer<ConsumedT> prev = this;

        private LinkedConsumer<ConsumedT> next = null;

        private final Subscription subscription = () -> remove();

        public LinkedConsumer() {
            this.delegate = (s, t) -> {};
        }


        public <U extends ConsumedT>LinkedConsumer(final BiConsumer<Subscription, ? super U> delegate) {
            this.delegate = (BiConsumer<Subscription, ? super ConsumedT>) delegate;
        }

        public void tryAcceptAll(final ConsumedT t) {

            LinkedConsumer<? super ConsumedT> current = this;

            do {
                final LinkedConsumer<? super ConsumedT> next = current.next;
                current.tryAccept(t);
                current = next;
            } while(current != null);

        }

        public void tryAccept(ConsumedT t) {
            try {
                delegate.accept(subscription, t);
            } catch (Exception ex) {
                logger.error("Unexpected exception dispatching consumer.", ex);
            }
        }


        public <U extends ConsumedT>
        LinkedConsumer<ConsumedT> andThenTry(final BiConsumer<Subscription, ? super U> next) {
            return andThenTry(new LinkedConsumer<>(next));
        }

        public LinkedConsumer<ConsumedT> andThenTry(final LinkedConsumer<ConsumedT> next) {
            if (this.next != null) throw new IllegalStateException();
            this.next = next;
            next.prev = this;
            return next;
        }

        public void remove() {
            if (prev != null) prev.next = next;
            if (next != null) next.prev = prev;
            next = null;
            prev = null;
        }

        public void clear() {
            next = null;
            prev = null;
        }

    }

}

package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Subscription;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.System.identityHashCode;

/**
 * A thread-safe {@link Publisher} which uses an internal linked list of {@link AtomicReference} instances to track the
 * subscriber list. This uses a per-node locking strategy in which only the nodes necessary to perform the mutation
 * are affected while mutating the list.
 *
 * However, when iterating the list of subscribers (such as when dispatching events) no locks are required as iteration
 * of the list can happen without any locks. Due to this design, however, it is possible that there is some
 * inconsistency. For example, a subscriber can be servicing an event in one thread as it is being removed. However,
 * in all cases, the list of subscribers will be eventaully consistent.
 *
 * @param <T>
 */
public class ConcurrentLinkedPublisher<T> extends AbstractPublisher<T> implements Iterable<Subscription> {

    private final AtomicReference<ListAnchor> anchor = new AtomicReference<>(new ListAnchor());

    public ConcurrentLinkedPublisher() {
        super(ConcurrentLockedPublisher.class);
    }

    public ConcurrentLinkedPublisher(final Class<?> cls) {
        super(cls);
    }

    public ConcurrentLinkedPublisher(final Logger logger) {
        super(logger);
    }

    @Override
    public Iterator<Subscription> iterator() {
        return anchor.get().iterator();
    }

    @Override
    public Subscription subscribe(final BiConsumer<Subscription, ? super T> consumer) {
        return anchor.get().subscribe(consumer);
    }

    @Override
    public void clear() {
        anchor.set(new ListAnchor());
    }

    @Override
    public void publish(final T t, Consumer<? super T> onFinish, final Consumer<Throwable> onException) {
        anchor.get().publish(t, onFinish, onException);
    }

    private class ListAnchor implements Iterable<Subscription> {

        private final ListNode first = new ListNode() {

            @Override
            public void tryAccept(final T t, final Consumer<Throwable> onException) {

                var current = getNext();

                while (current != null) {
                    final var next = current.getNext();
                    current.tryAccept(t, onException);
                    current = next;
                }

            }

            @Override
            public String toString() {
                return "LockedLinkedConsumer Anchor@"+ identityHashCode(this);
            }

            @Override
            public void unsubscribe() {
                // Here as a simple safeguard against removal of the first node in the list.
                // Since this will only happen during iteration.
                throw new UnsupportedOperationException("Cannot remove anchor subscription.");
            }

        };

        private final AtomicReference<ListNode> last = new AtomicReference<>(first);

        @Override
        public Iterator<Subscription> iterator() {
            return new Iterator<>() {

                private ListNode current = first;

                @Override
                public boolean hasNext() {
                    return current != null && current != first;
                }

                @Override
                public Subscription next() {
                    return (current = current.getNext());
                }

            };
        }

        public void publish(final T t, Consumer<? super T> onFinish, final Consumer<Throwable> onException) {

            first.tryAccept(t, onException);

            try {
                onFinish.accept(t);
            } catch (Exception ex) {
                handleException(onException, ex);
            }

        }

        public Subscription subscribe(final BiConsumer<Subscription, ? super T> consumer) {

            final var subscription = new ListNode(consumer);

            final var last = findAndLockLast();
            subscription.lock.lock();

            try {
                subscription.setPrev(last);
                last.setNext(subscription);
                this.last.set(subscription);
            } finally {
                subscription.lock.unlock();
                last.lock.unlock();
            }

            return subscription;

        }

        private ListNode findAndLockLast() {

            final var toUnlock = new LinkedList<ListNode>();

            try {
                while (true) {

                    final var last = this.last.get();
                    last.lock.lock();

                    if (last.getNext() == null) {
                        return last;
                    } else {
                        toUnlock.add(last);
                    }

                }
            } finally {
                toUnlock.forEach(listNode -> listNode.lock.unlock());
            }

        }

        private class ListNode implements Subscription {

            private final Lock lock = new ReentrantLock();

            public final BiConsumer<Subscription, ? super T> delegate;

            public final AtomicBoolean marked = new AtomicBoolean(false);

            public final AtomicReference<NodePair> pair = new AtomicReference<>();

            public ListNode() {
                this.delegate = (s, t) -> {};
            }

            public ListNode(final BiConsumer<Subscription, ? super T> delegate) {
                this.delegate = delegate;
            }

            public ListNode getPrev() {
                final var pair = this.pair.get();
                return pair == null ? null : pair.prev();
            }

            public void setPrev(final ListNode prev) {
                pair.set(new NodePair(prev, getNext()));
            }

            public ListNode getNext() {
                final var pair = this.pair.get();
                return pair == null ? null : pair.next();
            }

            public void setNext(final ListNode next) {
                pair.set(new NodePair(getPrev(), next));
            }

            @Override
            public void unsubscribe() {
                if (marked.compareAndSet(false, true)) {

                    final var pair = this.pair.get();
                    final var prev = pair.prev();
                    final var next = pair.next();

                    prev.lock.lock();
                    this.lock.lock();

                    if (next != null) {
                        next.lock.lock();
                    }

                    try {

                        prev.pair.set(new NodePair(prev.getPrev(), next));

                        if (next != null) {
                            next.pair.set(new NodePair(prev, next.getNext()));
                        }

                    } finally {

                        prev.lock.unlock();
                        this.lock.unlock();

                        if (next != null) {
                            next.lock.unlock();
                        }

                    }

                } else {
                    logger.error("Already unsubscribed.");
                }
            }

            public void tryAccept(final T t, final Consumer<Throwable> onException) {
                try {
                    if (!marked.get()) {
                        delegate.accept(this, t);
                    }
                } catch (Exception ex) {
                    handleException(onException, ex);
                }
            }

            private final class NodePair {

                private final ListNode prev;
                private final ListNode next;

                private NodePair(final ListNode prev, final ListNode next) {
                    this.prev = prev;
                    this.next = next;
                }

                public ListNode prev() {
                    return prev;
                }

                public ListNode next() {
                    return next;
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) return true;
                    if (obj == null || obj.getClass() != this.getClass()) return false;
                    var that = (NodePair) obj;
                    return Objects.equals(this.prev, that.prev) &&
                            Objects.equals(this.next, that.next);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(prev, next);
                }

                @Override
                public String toString() {
                    return "NodePair[" +
                            "getPrev=" + prev + ", " +
                            "getNext=" + next + ']';
                }
            }

        }

    }

}

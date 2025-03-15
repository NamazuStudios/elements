package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.ElementLoaderFactory.ClassLoaderConstructor;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.exception.SdkMultiException;
import dev.getelements.elements.sdk.util.ConcurrentDequePublisher;
import dev.getelements.elements.sdk.util.ConcurrentLinkedPublisher;
import dev.getelements.elements.sdk.util.Publisher;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.ClassLoader.getSystemClassLoader;

/**
 * Implements {@link ElementRegistry} at the root level with no parent. This should be used by the application at the
 * top-level. Note that when registering
 */
public class RootElementRegistry implements ElementRegistry {

    private boolean open = true;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final List<LoadedElement> loaded = new CopyOnWriteArrayList<>();

    private final Publisher<Event> onEventPublisher = new ConcurrentDequePublisher<>(RootElementRegistry.class);

    private final Publisher<ElementRegistry> onClosePublisher = new ConcurrentDequePublisher<>(RootElementRegistry.class);

    @Override
    public Stream<Element> stream() {
        return loaded.stream().map(LoadedElement::element);
    }

    @Override
    public Element register(final Element element) {

        final var lock = rwLock.writeLock();
        lock.lock();
        check();

        if (loaded.contains(element)) {
            throw new IllegalArgumentException("Element already exists: " + element);
        }

        try {

            final var subscriptions = Subscription.begin()
                    .chain(onEventPublisher.subscribe(element::publish))
                    .chain(onClosePublisher.subscribe(_this -> element.close()));

            final var loadedElement = new LoadedElement(element, subscriptions);
            loaded.add(loadedElement);

            return element;

        } finally {
            lock.unlock();
        }

    }

    @Override
    public boolean unregister(final Element element) {

        final var lock = rwLock.writeLock();
        lock.lock();
        check();

        try {
            return loaded.removeIf(loadedElement -> loadedElement.element() == element);
        } finally {
            lock.unlock();
        }

    }

    @Override
    public ElementRegistry newSubordinateRegistry() {
        return new RootElementRegistry() {

            private final RootElementRegistry parent = RootElementRegistry.this;

            private final Subscription subscription = Subscription.begin()
                    .chain(parent.onClose(p -> close()))
                    .chain(parent.onEvent(this::publish));

            @Override
            public void close() {
                super.close();
                subscription.unsubscribe();
            }

        };
    }

    @Override
    public void publish(final Event event) {
        onEventPublisher.publish(event);
    }

    @Override
    public Subscription onEvent(final Consumer<Event> onEvent) {
        return onEventPublisher.subscribe(onEvent);
    }

    @Override
    public Subscription onClose(final Consumer<ElementRegistry> onClose) {
        return onClosePublisher.subscribe(onClose);
    }

    private void check() {
        if (!open) throw new IllegalStateException("Registry is closed.");
    }

    @Override
    public void close() {

        final var lock = rwLock.writeLock();
        lock.lock();

        try {

            if (open) {

                final var causes = new LinkedList<Throwable>();
                onClosePublisher.publish(this, _t -> {}, causes::add);
                onClosePublisher.clear();

                if (!causes.isEmpty()) {
                    throw new SdkMultiException("Caught exception closing.", causes);
                }

            }

        } finally {
            open = false;
            lock.unlock();
        }
    }

    /**
     * Performs the load operation, allowing subclasses to specify an alternate {@link ElementRegistry} if necessary.
     *
     * @param loader the loader
     * @return the {@link Element} created from the loader
     */
    protected Element doLoad(final ElementLoader loader) {
        return loader.load(this);
    }

    private record LoadedElement(Element element, Subscription subscriptions) {}

}

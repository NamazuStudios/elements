package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.sdk.exception.SdkMultiException;
import dev.getelements.elements.sdk.util.ConcurrentDequePublisher;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.sdk.util.Publisher;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Implements {@link ElementRegistry} at the root level with no parent. This should be used by the application at the
 * top-level. Note that when registering
 */
public class RootElementRegistry implements ElementRegistry {

    private boolean open = true;

    private final Supplier<Stream<Element>> parentElementSupplier;

    private final Lock lock = new ReentrantLock();

    private final List<LoadedElement> loaded = new CopyOnWriteArrayList<>();

    private final Publisher<Event> onEventPublisher = new ConcurrentDequePublisher<>(RootElementRegistry.class);

    private final Publisher<ElementRegistry> onClosePublisher = new ConcurrentDequePublisher<>(RootElementRegistry.class);

    public RootElementRegistry() {
        this.parentElementSupplier = Stream::empty;
    }

    public RootElementRegistry(final Supplier<Stream<Element>> parentElementSupplier) {
        this.parentElementSupplier = parentElementSupplier;
    }

    @Override
    public Stream<Element> stream() {
        return Stream.concat(
                loaded.stream().map(LoadedElement::element),
                parentElementSupplier.get()
        );
    }

    @Override
    public Element register(final Element element) {


        try (final var mon = Monitor.enter(lock)) {

            check();

            if (loaded.stream().anyMatch(e -> e.element.equals(element))) {
                throw new IllegalArgumentException("Element already exists: " + element);
            }

            final var subscriptions = Subscription.begin()
                    .chain(onEvent(element::publish))
                    .chain(onClose(_this -> element.close()));

            final var loadedElement = new LoadedElement(element, subscriptions);
            loaded.add(loadedElement);

            return element;

        }

    }

    @Override
    public boolean unregister(final Element element) {
        try (final var mon = Monitor.enter(lock)) {

            check();

            final var iterator = loaded.iterator();

            while (iterator.hasNext()) {
                final var loadedElement = iterator.next().element();
                if (element.equals(element)) {
                    iterator.remove();
                    return true;
                }
            }

            return false;

        }
    }

    @Override
    public ElementRegistry newSubordinateRegistry() {
        final var subordinate = new RootElementRegistry(this::stream);
        final var subscription = Subscription.begin()
                .chain(onClose(p -> subordinate.close()))
                .chain(onEvent(subordinate::publish));
        subordinate.onClose(s -> subscription.unsubscribe());
        return subordinate;
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
        try (final var mon = Monitor.enter(lock)) {

            if (open) {

                final var causes = new LinkedList<Throwable>();
                onClosePublisher.publish(this, _t -> {}, causes::add);
                onClosePublisher.clear();
                open = false;

                if (!causes.isEmpty()) {
                    throw new SdkMultiException("Caught exception closing.", causes);
                }

            }

        }
    }
    
    private record LoadedElement(Element element, Subscription subscriptions) {}

}

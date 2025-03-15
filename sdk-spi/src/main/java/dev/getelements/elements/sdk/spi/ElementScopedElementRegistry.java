package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.*;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Provides a registry where a {@link Element} may be registered. Because each {@link Element} runs inside its own
 * isolated {@link ClassLoader}, there may be multiple instances of this type. However, from the perspective of a
 * {@link Element}, there is only one. The {@link ElementRegistry} also forms a hierarchy in which the element may load
 * subordinate {@link Element}s which are equally isolated.
 */
public final class ElementScopedElementRegistry implements ElementRegistry {

    private final Element element;

    private final RootElementRegistry delegate;

    private final Subscription subscriptions;

    public ElementScopedElementRegistry(final ElementRegistry parent, final Element element) {

        this.delegate = new RootElementRegistry() {

            @Override
            protected Element doLoad(final ElementLoader loader) {
                return loader.load(ElementScopedElementRegistry.this);
            }

        };

        this.element = element;
        this.delegate.register(element);
        this.subscriptions = Subscription.begin()
                .chain(parent.onClose(p -> close()))
                .chain(parent.onEvent(delegate::publish));

    }

    @Override
    public Stream<Element> stream() {
        return delegate.stream();
    }

    @Override
    public Element register(final Element element) {
        return delegate.register(element);
    }

    @Override
    public boolean unregister(final Element element) {

        if (this.element == element) {
            throw new IllegalArgumentException("Cannot unregister current Element.");
        }

        return delegate.unregister(element);

    }

    @Override
    public ElementRegistry newSubordinateRegistry() {
        return delegate.newSubordinateRegistry();
    }

    @Override
    public void publish(final Event event) {
        delegate.publish(event);
    }

    @Override
    public Subscription onEvent(final Consumer<Event> onEvent) {
        return delegate.onEvent(onEvent);
    }

    @Override
    public Subscription onClose(final Consumer<ElementRegistry> onClose) {
        return delegate.onClose(onClose);
    }

    @Override
    public void close() {
        delegate.close();
        subscriptions.unsubscribe();
    }

}

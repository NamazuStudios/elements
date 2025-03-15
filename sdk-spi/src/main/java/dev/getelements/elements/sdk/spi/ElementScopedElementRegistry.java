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

    private ElementRegistry parent;

    private final Subscription subscriptions;

    public ElementScopedElementRegistry(final ElementRegistry parent, final Element element) {
        this.parent = parent;
        this.element = element;
        this.subscriptions = parent.onClose(p -> close());
    }

    @Override
    public Stream<Element> stream() {
        return parent.stream();
    }

    @Override
    public MutableElementRegistry newSubordinateRegistry() {
        return parent.newSubordinateRegistry();
    }

    @Override
    public void publish(final Event event) {
        parent.publish(event);
    }

    @Override
    public Subscription onEvent(final Consumer<Event> onEvent) {
        return parent.onEvent(onEvent);
    }

    @Override
    public Subscription onClose(final Consumer<ElementRegistry> onClose) {
        return parent.onClose(onClose);
    }

    @Override
    public void close() {
        subscriptions.unsubscribe();
    }

}

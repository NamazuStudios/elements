package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.exception.SdkServiceNotFoundException;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementServiceKey;
import dev.getelements.elements.sdk.util.ConcurrentLinkedPublisher;
import dev.getelements.elements.sdk.util.Publisher;

import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class SharedElement implements Element {

    private final ElementRecord elementRecord;

    private final ServiceLocator serviceLocator;

    private final ElementRegistry elementRegistry;

    private final ElementEventDispatcher elementEventDispatcher;

    private final Publisher<Event> elementEventPublisher = new ConcurrentLinkedPublisher<>(SharedElement.class);

    public SharedElement(final ElementRecord elementRecord,
                         final ServiceLocator serviceLocator,
                         final ElementRegistry parent) {

        final var serviceKeySet = elementRecord
                .services()
                .stream()
                .flatMap(ElementServiceKey::from)
                .collect(toUnmodifiableSet());

        this.elementRecord = elementRecord;
        this.elementRegistry = new ElementScopedElementRegistry(parent, this);
        this.serviceLocator = new FilteredServiceLocator(serviceLocator, serviceKeySet);
        this.elementEventDispatcher = new ElementEventDispatcher(
                elementRecord,
                serviceLocator,
                elementEventPublisher
        );

    }

    @Override
    public ElementRecord getElementRecord() {
        return elementRecord;
    }

    @Override
    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    @Override
    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Override
    public void publish(final Event event) {
        elementEventPublisher.publish(event);
    }

    @Override
    public void close() {
        elementEventDispatcher.close();
    }

}

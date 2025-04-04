package dev.getelements.elements.sdk.spi.guice;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.spi.ElementEventDispatcher;
import dev.getelements.elements.sdk.util.ConcurrentDequePublisher;
import dev.getelements.elements.sdk.util.Publisher;
import jakarta.inject.Inject;

import java.util.Optional;

public class GuiceSdkElement implements Element {

    private final ElementRecord elementRecord;

    private final ServiceLocator serviceLocator;

    private final ElementRegistry elementRegistry;

    private final ElementEventDispatcher elementEventDispatcher;

    private final Publisher<Event> elementEventPublisher = new ConcurrentDequePublisher<>(GuiceSdkElement.class);

    @Inject
    public GuiceSdkElement(final ElementRecord elementRecord,
                           final ServiceLocator serviceLocator,
                           final ElementRegistry elementRegistry) {
        this.elementRecord = elementRecord;
        this.serviceLocator = serviceLocator;
        this.elementRegistry = elementRegistry;
        this.elementEventDispatcher = new ElementEventDispatcher(elementRecord, serviceLocator, elementEventPublisher);
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
    public ElementScope.Builder withScope() {
        return null;
    }

    @Override
    public Optional<ElementScope> findCurrentScope() {
        return Optional.empty();
    }

    @Override
    public void publish(Event event) {
        elementEventPublisher.publish(event);
    }

    @Override
    public void close() {

        final var cl = getElementRecord().classLoader();

        if (cl instanceof AutoCloseable) {
            try {
                ((AutoCloseable) cl).close();
            } catch (Exception ex) {
                throw new SdkException(ex);
            }
        }

        elementEventDispatcher.close();

    }

}

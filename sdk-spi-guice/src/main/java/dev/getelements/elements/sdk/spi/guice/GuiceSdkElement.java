package dev.getelements.elements.sdk.spi.guice;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.spi.DefaultElementScope;
import dev.getelements.elements.sdk.spi.DefaultElementScopeBuilder;
import dev.getelements.elements.sdk.spi.ElementEventDispatcher;
import dev.getelements.elements.sdk.util.ConcurrentDequePublisher;
import dev.getelements.elements.sdk.util.Publisher;
import dev.getelements.elements.sdk.util.ReentrantThreadLocal;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.function.Consumer;

public class GuiceSdkElement implements Element {

    private final ElementRecord elementRecord;

    private final ServiceLocator serviceLocator;

    private final ElementRegistry elementRegistry;

    private final ElementEventDispatcher elementEventDispatcher;

    private final Publisher<Event> elementEventPublisher = new ConcurrentDequePublisher<>(GuiceSdkElement.class);

    private final Publisher<Element> onClosePublisher = new ConcurrentDequePublisher<>(GuiceSdkElement.class);

    private final ReentrantThreadLocal<DefaultElementScope> scopeThreadLocal = new ReentrantThreadLocal<>();

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
        final var attributes = getElementRecord().attributes();
        return new DefaultElementScopeBuilder(attributes, scopeThreadLocal);
    }

    @Override
    public Optional<ElementScope> findCurrentScope() {
        return scopeThreadLocal
                .getCurrentOptional()
                .map(ElementScope.class::cast);
    }

    @Override
    public void publish(Event event) {
        elementEventPublisher.publish(event);
    }

    @Override
    public Subscription onClose(final Consumer<Element> onClose) {
        return onClosePublisher.subscribe(onClose);
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

        onClosePublisher.publish(this);
        onClosePublisher.clear();
        elementEventDispatcher.close();

    }

}

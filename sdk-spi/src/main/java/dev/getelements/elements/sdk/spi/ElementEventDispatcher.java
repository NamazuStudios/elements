package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.sdk.exception.EventDispatchException;
import dev.getelements.elements.sdk.record.ElementEventConsumerRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.util.LinkedPublisher;
import dev.getelements.elements.sdk.util.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;

public class ElementEventDispatcher implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ElementEventDispatcher.class);

    private final Subscription subscriptions;

    private final ElementRecord elementRecord;

    private final ServiceLocator serviceLocator;

    private final Publisher<Event> noop = new LinkedPublisher<>();

    public ElementEventDispatcher(final ElementRecord elementRecord,
                                  final ServiceLocator serviceLocator,
                                  final Publisher<Event> eventPublisher) {

        this.elementRecord = elementRecord;
        this.serviceLocator = serviceLocator;
        this.subscriptions = Subscription.begin()
                .chain(buildDirectSubscriptions(eventPublisher))
                .chain(buildParameterMatchedSubscriptions(eventPublisher));

        if (logger.isDebugEnabled()) {
            noop.subscribe(event -> logger.debug("Element {} has unconsumed event {} [{}].",
                    getElementRecord().definition().name(),
                    event.getEventName(),
                    event.getEventArguments()
                            .stream()
                            .map(Objects::toString)
                            .collect(joining(","))));
        }

    }

    private Subscription buildDirectSubscriptions(final Publisher<Event> eventPublisher) {

        final Map<DirectKey, Publisher<Event>> directDispatchers = new HashMap<>();

        elementRecord
                .consumedEvents()
                .stream()
                .filter(ElementEventConsumerRecord::isDirectDispatch)
                .forEach(consumer -> {

                    final var key = DirectKey.from(consumer);
                    final Consumer<Event> dispatcher = Modifier.isStatic(consumer.method().getModifiers())
                            ? event -> dispatchDirectStatic(consumer, event)
                            : event -> dispatchDirectInstance(consumer, event);

                    directDispatchers
                            .computeIfAbsent(key, k -> new LinkedPublisher<>())
                            .subscribe(dispatcher);

                });

        return eventPublisher.subscribe(event -> {
            final var key = DirectKey.from(event);
            directDispatchers.getOrDefault(key, noop).publish(event);
        });
    }

    private void dispatchDirectStatic(final ElementEventConsumerRecord<?> consumer, final Event event) {
        try {
            consumer.method().invoke(null, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new EventDispatchException(e);
        }
    }

    private void dispatchDirectInstance(final ElementEventConsumerRecord<?> consumer, final Event event) {

        final var service = serviceLocator.findInstance(consumer.eventKey().serviceKey());

        if (service.isPresent()) {
            try {
                consumer.method().invoke(service.get().get(), event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new EventDispatchException(e);
            }
        } else {
            logger.warn("Unable to locate service: {}: ", consumer.eventKey().serviceKey());
        }

    }

    private Subscription buildParameterMatchedSubscriptions(final Publisher<Event> eventPublisher) {

        final Map<ParameterKey, Publisher<Event>> matchedDispatchers = new HashMap<>();

        elementRecord
                .consumedEvents()
                .stream()
                .filter(Predicate.not(ElementEventConsumerRecord::isDirectDispatch))
                .forEach(consumer -> {

                    final var key = ParameterKey.from(consumer);
                    final Consumer<Event> dispatcher = Modifier.isStatic(consumer.method().getModifiers())
                            ? event -> dispatchMatchedStatic(consumer, event)
                            : event -> dispatchMatchedInstance(consumer, event);

                    matchedDispatchers
                            .computeIfAbsent(key, k -> new LinkedPublisher<>())
                            .subscribe(dispatcher);

                });

        return eventPublisher.subscribe(event -> {
            if (event.getEventArguments().stream().allMatch(Objects::nonNull)) {
                final var key = ParameterKey.from(event);
                matchedDispatchers.getOrDefault(key, noop).publish(event);
            } else {
                logger.warn("Event {} has null arguments [{}]. Refusing to match.",
                        event.getEventName(),
                        event.getEventArguments()
                                .stream()
                                .map(Objects::toString)
                                .collect(joining(","))
                );
            }
        });

    }

    private void dispatchMatchedStatic(final ElementEventConsumerRecord<?> consumer, final Event event) {
        try {
            consumer.method().invoke(null, event.getEventArguments().toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new EventDispatchException(e);
        }
    }

    private void dispatchMatchedInstance(final ElementEventConsumerRecord<?> consumer, final Event event) {

        final var service = serviceLocator.findInstance(consumer.eventKey().serviceKey());

        if (service.isPresent()) {
            try {
                consumer.method().invoke(service.get().get(), event.getEventArguments().toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new EventDispatchException(e);
            }
        } else {
            logger.warn("Unable to locate service: {}: ", consumer.eventKey().serviceKey());
        }

    }

    @Override
    public void close() {
        subscriptions.unsubscribe();
    }

    private ElementRecord getElementRecord() {
        return elementRecord;
    }

    private record DirectKey(String name) {

        public static DirectKey from(ElementEventConsumerRecord<?> record) {
            return new DirectKey(record.eventKey().eventName());
        }

        public static DirectKey from(final Event event) {

            final List<Class<?>> list = event
                    .getEventArguments()
                    .stream()
                    .map(Object::getClass)
                    .collect(toUnmodifiableList());

            return new DirectKey(event.getEventName());

        }

    }

    private record ParameterKey(String name, List<Class<?>> cls) {

        public static ParameterKey from(ElementEventConsumerRecord<?> record) {
            final var list = List.of(record.method().getParameterTypes());
            return new ParameterKey(record.eventKey().eventName(), list);
        }

        public static ParameterKey from(final Event event) {

            final List<Class<?>> list = event
                    .getEventArguments()
                    .stream()
                    .map(Object::getClass)
                    .collect(toUnmodifiableList());

            return new ParameterKey(event.getEventName(), list);

        }

    }

}

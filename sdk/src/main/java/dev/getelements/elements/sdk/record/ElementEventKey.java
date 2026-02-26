package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementEventConsumer;

/**
 * Represents a unique key for identifying an event consumer, combining both the service that will
 * handle the event and the name of the event being consumed. This key is used to register and
 * route events to the appropriate service instances.
 *
 * <p>An event key consists of:</p>
 * <ul>
 *     <li><strong>Service Key</strong> ({@link #serviceKey()}): Identifies which service instance
 *         should receive the event, including the service type and optional name qualifier.</li>
 *     <li><strong>Event Name</strong> ({@link #eventName()}): The name of the event being consumed,
 *         as specified in {@link ElementEventConsumer#value()}.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * Event keys are typically created from {@link ElementEventConsumer} annotations during the
 * scanning and registration phase. The key determines how events are routed through the system:
 *
 * <pre>{@code
 * // From an annotation with explicit via routing
 * @ElementEventConsumer(
 *     value = "my.event",
 *     via = @ElementServiceReference(MyService.class)
 * )
 * // Creates: ElementEventKey(serviceKey=ElementServiceKey(MyService.class, ""), eventName="my.event")
 * }</pre>
 *
 * @param <ServiceT> the type of service that will handle the event
 * @param serviceKey the service key identifying which service instance receives the event
 * @param eventName the name of the event being consumed
 * @see ElementEventConsumer
 * @see ElementServiceKey
 * @since 3.7
 */
public record ElementEventKey<ServiceT>(ElementServiceKey<ServiceT> serviceKey, String eventName) {

    /**
     * Creates an event key from an {@link ElementEventConsumer} annotation that specifies explicit
     * service routing via the {@link ElementEventConsumer#via()} field.
     *
     * <p>This method extracts the service reference from the annotation's {@code via} field and
     * combines it with the event name to create a complete event key. The {@code via} field must
     * be specified (i.e., not {@link ElementEventConsumer.None}).</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * @ElementEventConsumer(
     *     value = "my.event",
     *     via = @ElementServiceReference(MyService.class)
     * )
     * public void onEvent() { }
     *
     * // Creates key: ElementEventKey(serviceKey=ElementServiceKey(MyService.class, ""), eventName="my.event")
     * }</pre>
     *
     * @param elementEventConsumer the event consumer annotation with a specified {@code via} field
     * @return an event key combining the service reference and event name
     * @throws IllegalArgumentException if the {@code via} field is not specified (defaults to {@link ElementEventConsumer.None})
     * @see #from(ElementServiceKey, ElementEventConsumer)
     */
    public static ElementEventKey<?> from(final ElementEventConsumer elementEventConsumer) {

        final var serviceReference = elementEventConsumer.via();

        if (ElementEventConsumer.None.class.equals(serviceReference.value())) {
            throw new IllegalArgumentException("Element event consumer via field not specified.");
        }

        final var elementServiceKey = ElementServiceKey.from(serviceReference);

        return from(elementServiceKey, elementEventConsumer);

    }


    /**
     * Creates an event key from an existing service key and an {@link ElementEventConsumer} annotation.
     * This method is used when the service key has already been determined (for example, from the
     * declaring class of the event consumer method) rather than from the annotation's {@code via} field.
     *
     * <p>This is the typical path for direct event consumption where the service key comes from the
     * class that declares the event consumer method, not from explicit routing.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * // Service class with direct event consumption
     * @ElementServiceExport
     * public class MyServiceImpl {
     *     @ElementEventConsumer("my.event")
     *     public void onEvent() { }
     * }
     *
     * // Service key derived from MyServiceImpl
     * ElementServiceKey<MyServiceImpl> serviceKey = ...;
     * // Combined with annotation to create event key
     * ElementEventKey<MyServiceImpl> eventKey = ElementEventKey.from(serviceKey, annotation);
     * }</pre>
     *
     * @param <ServiceT> the type of service that will handle the event
     * @param serviceKey the service key identifying the service instance
     * @param elementEventConsumer the event consumer annotation providing the event name
     * @return an event key combining the provided service key and the event name from the annotation
     * @see #from(ElementEventConsumer)
     */
    public static <ServiceT> ElementEventKey<ServiceT> from(
            final ElementServiceKey<ServiceT> serviceKey,
            final ElementEventConsumer elementEventConsumer) {
        return new ElementEventKey<>(serviceKey, elementEventConsumer.value());
    }

}

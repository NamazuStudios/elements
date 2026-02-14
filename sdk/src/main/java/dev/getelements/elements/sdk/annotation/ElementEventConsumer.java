package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.ServiceLocator;

import java.lang.annotation.*;

/**
 * Annotates a method capable of receiving an instance of {@link Event}. When an event is published,
 * the runtime will invoke this method with the event data. If the method's parameters match the types
 * of {@link Event#getEventArguments()}, the method will receive the unpacked arguments directly.
 *
 * <h3>Direct Event Consumption</h3>
 * For classes annotated with {@link ElementServiceExport}, event consumer methods are automatically discovered
 * and invoked when matching events are published. The service locator will find the service instance and
 * dispatch the event to it.
 *
 * <pre>{@code
 * @ElementServiceExport
 * public class MyService {
 *     @ElementEventConsumer("my.event")
 *     public void onMyEvent(String arg1, int arg2) {
 *         // Called when "my.event" is published with matching arguments
 *     }
 * }
 * }</pre>
 *
 * <h3>Routed Event Consumption</h3>
 * For implementation classes that are not directly exposed as services, use the {@link #via()} field to
 * route events through an exported service interface. This allows internal implementation classes to receive
 * events without needing to be part of the public API.
 *
 * <pre>{@code
 * // Public service interface
 * @ElementPublic
 * public interface MyService {
 *     // Service methods
 * }
 *
 * // Private implementation that receives events
 * public class MyServiceImpl implements MyService {
 *     @ElementEventConsumer(
 *         value = "my.event",
 *         via = @ElementServiceReference(MyService.class)
 *     )
 *     public void onMyEvent(String arg1, int arg2) {
 *         // Called when "my.event" is published
 *         // The service locator finds this instance via MyService interface
 *     }
 * }
 * }</pre>
 *
 * When using {@link #via()}, the service locator will:
 * <ol>
 *     <li>Look up the service instance using the specified service interface</li>
 *     <li>Cast the instance to the declaring class of the annotated method</li>
 *     <li>Invoke the method on that instance</li>
 * </ol>
 *
 * This is useful when you want to keep implementation details private while still participating in
 * the event system.
 *
 * @see Event
 * @see ServiceLocator
 * @see ElementServiceExport
 * @see ElementServiceReference
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ElementEventConsumers.class)
public @interface ElementEventConsumer {

    /**
     * The name of the {@link Event}.
     *
     * @return the name of the event
     */
    String value();

    /**
     * Specifies a service interface through which this event consumer should be invoked. When specified,
     * the {@link ServiceLocator} will look up the service instance using the provided service reference,
     * then invoke this method on that instance.
     *
     * <p>This allows implementation classes that are not directly exported to receive events by routing
     * through their public service interface. The implementation class must still be annotated with
     * {@link ElementServiceExport} and implement the interface specified in this field.</p>
     *
     * <p>If not specified (defaults to {@link None}), the event consumer is invoked directly on the
     * service instance, which requires the declaring class to be annotated with {@link ElementServiceExport}.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * @ElementServiceExport
     * public class MyServiceImpl implements MyPublicService {
     *     @ElementEventConsumer(
     *         value = "my.event",
     *         via = @ElementServiceReference(MyPublicService.class)
     *     )
     *     public void onEvent(String arg) {
     *         // Invoked via lookup of MyPublicService
     *     }
     * }
     * }</pre>
     *
     * @return the service reference to use for event routing, or {@code @ElementServiceReference(None.class)}
     *         to use direct invocation
     * @see ServiceLocator
     * @see ElementServiceReference
     * @since 3.7
     */
    ElementServiceReference via() default @ElementServiceReference(None.class);

    /**
     * Placeholder type used as the default value for {@link #via()} to indicate that no service routing
     * should be used. When {@code @ElementServiceReference(None.class)} is specified (the default),
     * the event consumer will be invoked directly on the service instance rather than routing through
     * another service interface.
     *
     * <p>This class cannot be instantiated and serves only as a marker type for annotation defaults.</p>
     *
     * @see #via()
     */
    class None { private None() {} }

}

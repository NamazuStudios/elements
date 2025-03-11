package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Event;

import java.lang.annotation.*;

/**
 * Annotates a method capable of receiving an instance of {@link Event}. Additionally, if the method's parameters match
 * the types of {@link Event#getEventArguments()}, then the method will receive the arguments of the event as well.
 *
 * Additionally, the type must be a service as specified by {@link ElementService} or annotated with
 * {@link ElementServiceExport} in order to receive events.
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

}

package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Event;

import java.lang.annotation.*;

/**
 * Used for documentation and reporting. This allows arbitrary definition of {@link Event} types.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ElementEventProducers.class)
public @interface ElementEventProducer {

    /**
     * The name of the {@link Event}.
     *
     * @return the name of the event
     */
    String value();

    /**
     * A brief description of the {@link Event}.
     *
     * @return the even description.
     */
    String description() default "";

    /**
     * The types of parameters the event will produce.
     */
    Class<?>[] parameters() default {};

}

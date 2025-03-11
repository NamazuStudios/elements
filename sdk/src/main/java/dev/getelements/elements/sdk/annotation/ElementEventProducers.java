package dev.getelements.elements.sdk.annotation;

import java.lang.annotation.*;


/**
 * {@link Repeatable} type for {@link ElementEventProducer}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementEventProducers {

    /**
     * The {@link ElementEventProducer} values.
     *
     * @return {@link ElementEventProducer} values
     */
    ElementEventProducer[] value() default {};

}

package dev.getelements.elements.sdk.annotation;


import java.lang.annotation.*;

/**
 * {@link Repeatable} type for {@link ElementEventConsumer}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementEventConsumers {

    /**
     * The {@link ElementEventConsumer} instances.
     *
     * @return {@link ElementEventConsumer} instances
     */
    ElementEventConsumer[] value() default {};

}

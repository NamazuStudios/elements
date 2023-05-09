package dev.getelements.elements.rt.annotation;

/**
 * Defines a return value from a {@link MethodDefinition}
 */
public @interface ReturnDefinition {

    /**
     * The documentation comment value of this return definition
     *
     * @return the comment
     */
    String comment() default "";

    /**
     * A string representing the type of return value.
     *
     * @return the type
     */
    String type() default "";

}

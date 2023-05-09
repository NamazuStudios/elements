package dev.getelements.elements.rt.annotation;

/**
 * Used to flag an {@link Intrinsic}-linked value with documentation stub. This does nothing more than simply provide
 * the stub's raw contents.
 */
public @interface MethodDefinition {

    /**
     * The method name.
     *
     * @return the method name
     */
    String value();

    /**
     * The summary of the method. A brief summary of the description.
     *
     * @return the summary
     */
    String summary() default "";

    /**
     * The description of the method.
     *
     * @return the description
     */
    String description() default "";

    /**
     * The return values, if any, of this {@link MethodDefinition}
     *
     * @return the {@link ReturnDefinition}s
     */
    ReturnDefinition[] returns() default {};

    /**
     * The parameter values, if any, of this {@link MethodDefinition}
     *
     * @return the {@link ReturnDefinition}s
     */
    ParameterDefinition[] parameters() default {};

}

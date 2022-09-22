package com.namazustudios.socialengine.rt.annotation;

/**
 * Defines a constant.
 */
public @interface FieldDefinition {

    /**
     * Type the value of the constant definition.
     *
     * @return the type.
     */
    String value();

    /**
     * The type of the constant.
     *
     * @return the type
     */
    String type() default "";

    /**
     * The literal value of the constant
     *
     * @return the literal value of the constant (if available)
     */
    String literal() default "";

    /**
     * The value of the description.
     */
    String description() default "";

}

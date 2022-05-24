package com.namazustudios.socialengine.rt.annotation;

import com.google.common.base.CaseFormat;

import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

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

    /**
     * The source case format. This defaults to {@link CaseFormat#UPPER_UNDERSCORE}.
     *
     * @return the source case format.
     */
    CaseFormat sourceCaseFormat() default UPPER_UNDERSCORE;

}

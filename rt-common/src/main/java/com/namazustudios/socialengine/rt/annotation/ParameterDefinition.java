package com.namazustudios.socialengine.rt.annotation;

public @interface ParameterDefinition {

    String value();

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

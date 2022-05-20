package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Represents an type which provides intrinsic APIs to the script engine, but does not actually use any sort of auto
 * binding system. This is a free-form module definition which will generate documentation stubs inline with the code
 * but does little translation to documentation tags.
 */
@Target(ElementType.TYPE)
public @interface Intrinsic {

    /**
     * The {@link ModuleDefinition}
     *
     * @return the {@link ModuleDefinition}
     */
    ModuleDefinition[] value();

    /**
     * Defines all authors of this {@link Intrinsic}
     *
     * @return the authors
     */
    String[] authors() default {};

    /**
     * The summary of the {@link Intrinsic}. A brief description of the intrinsic.
     *
     * @return the summary
     */
    String summary() default "";

    /**
     * The description for htis {@link Intrinsic}
     */
    String description() default "";

    /**
     * The methods defined by this value.
     *
     * @return the methods
     */
    MethodDefinition[] methods() default {};

    /**
     * The constants defined by this value.
     *
     * @return the constants
     */
    FieldDefinition[] constants() default {};

}

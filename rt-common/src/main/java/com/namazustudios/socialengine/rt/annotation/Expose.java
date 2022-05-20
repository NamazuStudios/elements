package com.namazustudios.socialengine.rt.annotation;


import java.lang.annotation.*;

/**
 * Used to expose specific types to the Resource instances where necessary.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expose {

    /**
     * The value of this {@link Expose} annotation. This lists out the modules to expose to the underlying services
     * which may need to make use of them.
     *
     * @return the value
     */
    ModuleDefinition[] value() default {};

    /**
     * The name of the lua module which will map to the object.
     *
     * @deprecated The {@link #value()} field provides a better definition.
     *
     * @return the module name
     */
    @Deprecated String[] modules() default {};

}

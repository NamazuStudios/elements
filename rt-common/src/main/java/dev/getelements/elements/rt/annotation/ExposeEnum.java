package dev.getelements.elements.rt.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to expose specific Enum types to the Resource instances where necessary.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExposeEnum {

    /**
     * The name of the lua module which will map to the Enum.
     *
     * @return the module name
     */
    ModuleDefinition[] value() default {};

    /**
     * The name of the lua module which will map to the Enum.
     *
     * @return the module name
     */
    String[] modules() default {};

}

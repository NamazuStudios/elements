package com.namazustudios.socialengine.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to expose specific types to the Resource instances where necessary.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expose {

    /**
     * The name of the lua module which will map to the object.
     *
     * @return the lua module name
     */
    String luaModuleName();

}

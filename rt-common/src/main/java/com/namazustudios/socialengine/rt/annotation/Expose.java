package com.namazustudios.socialengine.rt.annotation;


import java.lang.annotation.*;

/**
 * Used to expose specific types to the Resource instances where necessary.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expose {

    /**
     * The name of the lua module which will map to the object.
     *
     * @return the module name
     */
    String[] modules();

}

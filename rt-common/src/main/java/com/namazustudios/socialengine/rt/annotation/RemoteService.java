package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a type is a remotely-invokable service. This may be done with or without proxying.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteService {

    /**
     * Defines the various remote service definitions.
     *
     * @return all {@link RemoteServiceDefinition}s within this
     */
    RemoteServiceDefinition[] value();

}

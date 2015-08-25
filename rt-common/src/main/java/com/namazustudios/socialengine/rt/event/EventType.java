package com.namazustudios.socialengine.rt.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by patricktwohig on 8/25/15.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventType {

    /**
     * The name of the event.
     *
     * @return
     */
    String value() default "";

}

package com.namazustudios.socialengine.rt.annotation;

import com.namazustudios.socialengine.rt.Reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * Designates a method as being remotely invokable.  The method must be marked in a {@link Proxyable} type, and it may
 * provide {@link Routing} information if desired.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemotelyInvokable {

    /**
     * Specifies the routing options using the {@link Routing} annotation.
     *
     * @return the {@link Routing}
     */
    Routing routing() default @Routing;

    /**
     * Indicates the deprecation status of the method.
     *
     * @return the deprecation status
     */
    DeprecationDefinition deprecated() default @DeprecationDefinition(deprecated = false);

    final class Util {


        /**
         * Gets all {@link Method}
         * @param cls the {@link Class<?>} from hich to extract the methods.
         * @return a {@link Stream} of {@link Method} types.
         */
        public static Method[] getMethods(final Class<?> cls) {
            return getMethodStream(cls).toArray(Method[]::new);
        }

        /**
         * Gets all {@link Method}
         * @param cls the {@link Class<?>} from hich to extract the methods.
         * @return a {@link Stream} of {@link Method} types.
         */
        public static Stream<Method> getMethodStream(final Class<?> cls) {
            return Reflection.methods(cls).filter(m -> m.getAnnotationsByType(RemotelyInvokable.class).length > 0);
        }

    }

}

package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

/**
 * Houses some utility logic for interacting with the Reflection API.
 */
public class Reflection {

    /**
     * Streams a {@link Method}s in a {@link Class}.
     *
     * @param aClass a class
     * @return a {@link Stream<Method>}
     */
    public static Stream<Method> methods(final Class<?> aClass) {

        Stream<Method> methodStream = empty();

        for (Class<?> cls = aClass; cls != Object.class; cls = cls.getSuperclass()) {
            methodStream = concat(methodStream, stream(cls.getMethods()));
        }

        return methodStream;

    }

}

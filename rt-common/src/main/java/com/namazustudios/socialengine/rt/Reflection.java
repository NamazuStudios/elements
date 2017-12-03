package com.namazustudios.socialengine.rt;


import com.google.common.collect.Streams;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

/**
 * Houses some utility logic for interacting with the Reflection API.
 */
public class Reflection {

    /**
     * Formats a {@link Method} for use in logging.
     *
     * @param method the {@link Method}
     * @return the {@link String} representing the method
     */
    public static String format(final Method method) {
        final Class<?>[] args = method.getParameterTypes();
        final Class<?> declaringClass = method.getDeclaringClass();
        final String parameterSpec = stream(args).map(c -> c.getName()).collect(joining(","));
        return String.format("%s.%s(%s)", declaringClass.getName(), method.getName(), parameterSpec);
    }

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

    /**
     * Returns an {@link IllegalArgumentException} with a descriptive name for the method and paramters.
     *
     * @param cls the type
     * @param name the name of the method
     * @return the {@link IllegalArgumentException}
     */
    public static IllegalArgumentException noSuchMethod(final Class<?> cls, final String name) {
        final String msg = String.format("No such method: %s.%s()", cls.getName(), name);
        return new IllegalArgumentException(msg);
    }

    /**
     * Returns an {@link IllegalArgumentException} with a descriptive name for the method and paramters.
     *
     * @param cls the type
     * @param name the name of the method
     * @param args the argument types
     * @return the {@link IllegalArgumentException}
     */
    public static IllegalArgumentException noSuchMethod(final Class<?> cls, final String name, final Collection<Class<?>> args) {
        return noSuchMethod(cls, name, args.stream().toArray(Class[]::new));
    }

    /**
     * Returns an {@link IllegalArgumentException} with a descriptive name for the method and paramters.
     *
     * @param cls the type
     * @param name the name of the method
     * @param args the argument types
     * @return the {@link IllegalArgumentException}
     */
    public static IllegalArgumentException noSuchMethod(final Class<?> cls, final String name, final Class<?>[] args) {
        final String parameterSpec = stream(args).map(c -> c.getName()).collect(joining(","));
        final String msg = String.format("No such method: %s.%s(%s)", cls, name, parameterSpec);
        return new IllegalArgumentException(msg);
    }

}

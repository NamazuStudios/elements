package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.ErrorHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    /**
     * Finds the {@link Parameter} index annoated with {@link ErrorHandler} in the {@link Method}
     *
     * @param method the {@link Method}
     * @return the integer
     */
    public static int errorHandlerIndex(final Method method) {

        final Parameter[] parameters = method.getParameters();

        return IntStream
                .range(0, parameters.length)
                .filter(index -> parameters[index].getAnnotation(ErrorHandler.class) != null)
                .findFirst().orElse(-1);

    }

    /**
     * Returns the parameter indices of the {@link Method} which are anootated with the supplied annotation class.
     *
     * @param method the method
     * @param aClass the {@link Annotation} type
     * @return an integer array of all indices
     */
    public static int[] indices(final Method method, final Class<? extends Annotation> aClass) {

        final Parameter[] parameters = method.getParameters();

        return IntStream
            .range(0, parameters.length)
            .filter(index -> parameters[index].getAnnotation(aClass) != null)
            .toArray();

    }

    public static Method getHandlerMethod(final int index, final Method method, final Class<?> parameterType) {

        final Parameter parameter = method.getParameters()[index];
        final Class<?> type = parameter.getType();

        if (type.getAnnotation(FunctionalInterface.class) == null) {

            final String msg = "Parameter at index " + index + " " +
                    " in method " + format(method) +
                    " is not annotated with @FunctionalInterface";

            throw new IllegalArgumentException(msg);

        }

        final Method handlerMethod = methods(type)
                .filter(m -> !m.isDefault())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No non-default method found in type: " + type));

        if (handlerMethod.getParameterCount() != 1) {
            final String msg = format(handlerMethod) + " must accept a single parameter.";
            throw new IllegalArgumentException(msg);
        }

        final Parameter handlerParameter = handlerMethod.getParameters()[0];

        if (!parameterType.isAssignableFrom(handlerParameter.getType())) {
            final String msg = format(handlerMethod) + " must accept a Throwable.";
            throw new IllegalArgumentException(msg);
        }

        return handlerMethod;

    }

}

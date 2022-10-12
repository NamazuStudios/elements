package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.ErrorHandler;
import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
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

    private static final Logger logger = LoggerFactory.getLogger(Reflection.class);

    private Reflection(){}

    /**
     * Checks if the supplied {@link Class<?>} is a primitive number.
     * @param cls the class
     * @return true if the number is a primitive number
     */
    public static boolean isObjectInteger(final Class<?> cls) {
        return
            Byte.class.equals(cls) ||
            Short.class.equals(cls) ||
            Character.class.equals(cls) ||
            Integer.class.equals(cls) ||
            Long.class.equals(cls);
    }

    /**
     * Checks if the supplied {@link Class<?>} is a primitive number.
     * @param cls the class
     * @return true if the number is a primitive number
     */
    public static boolean isPrimitiveInteger(final Class<?> cls) {
        return
            byte.class.equals(cls) ||
            short.class.equals(cls) ||
            char.class.equals(cls) ||
            int.class.equals(cls) ||
            long.class.equals(cls);
    }

    /**
     * Checks if the supplied {@link Class<?>} is a primitive number.
     * @param cls the class
     * @return true if the number is a primitive number
     */
    public static boolean isObjectFloat(final Class<?> cls) {
        return Float.class.equals(cls) || Double.class.equals(cls);
    }

    /**
     * Checks if the supplied {@link Class<?>} is a primitive float.
     * @param cls the class
     * @return true if the number is a primitive number
     */
    public static boolean isPrimitiveFloat(final Class<?> cls) {
        return float.class.equals(cls) || double.class.equals(cls);
    }

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

        boolean isInterface = false;

        for (Class<?> cls = aClass; cls != null; cls = cls.getSuperclass()) {
            isInterface = cls.isInterface();
            methodStream = concat(methodStream, stream(cls.getMethods()));
        }

        return isInterface ? concat(methodStream, stream(Object.class.getMethods())) : methodStream;

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
    public static IllegalArgumentException noSuchMethod(
            final Class<?> cls,
            final String name,
            final Collection<Class<?>> args) {
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

    /**
     * Searches {@link Method}'s parameters for a handler type.
     *
     * @param parameter parameter
     * @return
     */
    public static Method getHandlerMethod(final Parameter parameter) {

        final Class<?> type = parameter.getType();

        if (type.getAnnotation(FunctionalInterface.class) == null) {

            final String msg =
                "Parameter type " + parameter +
                " is not annotated with " + FunctionalInterface.class.getName();

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

        if (!void.class.equals(handlerMethod.getReturnType())) {
            logger.warn("{} returns a value.", format(handlerMethod));
        }

        return handlerMethod;

    }

    /**
     * Gets the default value for the supplied type. For primitive types, this is always "0" and for non-primitive
     * types, this is null.
     *
     * @param parameter the {@link Parameter}
     * @return the default value
     */
    public static Object getDefaultValue(final Parameter parameter) {
        return getDefaultValue(parameter.getType());
    }

    /**
     * Gets the default value for the supplied type. For primitive types, this is always "0" and for non-primitive
     * types, this is null.
     *
     * @param type the type
     * @return the default value
     */
    public static Object getDefaultValue(final Class<?> type) {
        if (byte.class.equals(type)) {
            return (byte) 0;
        } else if (short.class.equals(type)) {
            return (short) 0;
        } else if (char.class.equals(type)) {
            return (char) 0;
        } else if (int.class.equals(type)) {
            return 0;
        } else if (long.class.equals(type)) {
            return 0L;
        } else if (float.class.equals(type)) {
            return 0f;
        } else if (double.class.equals(type)) {
            return 0;
        } else if (boolean.class.equals(type)) {
            return false;
        } else {
            return null;
        }
    }

    /**
     * Gets the default value for the supplied type. For primitive types, this is always "0" and for non-primitive
     * types, this is null.
     *
     * @param type the type
     * @return the default value
     */
    public static Object getBoxedPrimitive(final Class<?> type, final Object boxedPrimitive) {

        if (boxedPrimitive == null) {
            return getDefaultValue(type);
        }

        if (!type.isPrimitive()) {
            throw new IllegalArgumentException(type + " is not a primitive type.");
        }

        final Number number = boxedPrimitive instanceof Boolean
            ? Double.valueOf(0)
            : (Number) boxedPrimitive;

        if (byte.class.equals(type)) {
            return number.byteValue();
        } else if (short.class.equals(type)) {
            return number.shortValue();
        } else if (char.class.equals(type)) {
            return (char) number.shortValue();
        } else if (int.class.equals(type)) {
            return number.intValue();
        } else if (long.class.equals(type)) {
            return number.longValue();
        } else if (float.class.equals(type)) {
            return number.floatValue();
        } else if (double.class.equals(type)) {
            return number.doubleValue();
        } else if (boolean.class.equals(type)) {
            return number.doubleValue() != 0;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }

    }

    public static int count(final Method method, final Class<? extends Annotation> annotationClass) {
        return Stream.of(method.getParameters())
            .filter(p -> p.getAnnotation(annotationClass) != null)
            .mapToInt(p -> 1)
            .reduce(0, Integer::sum);
    }

}

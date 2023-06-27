package dev.getelements.elements.rt.annotation;

import java.lang.annotation.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 * Default {@link ExposedBindingAnnotation.BindingAnnotationFactory}. This supports a simple annotation type which has
 * no value, fields, or members. This uses the {@link Proxy} utility to create the instance with default behavior.
 */
public class DefaultBindingAnnotationFactory implements ExposedBindingAnnotation.BindingAnnotationFactory {

    private static final Set<Method> METHODS;

    static {
        try {

            final var methods = new HashSet<>(asList(
                Annotation.class.getMethod("toString"),
                Annotation.class.getMethod("annotationType"),
                Annotation.class.getMethod("equals", Object.class),
                Annotation.class.getMethod("hashCode")
            ));

            METHODS = unmodifiableSet(methods);

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Annotation construct(final Class<?> cls, final ExposedBindingAnnotation definition) {

        try {

            final var type = definition.value();
            final var methods = new HashSet<>(asList(type.getMethods()));
            if (!METHODS.equals(methods)) throw new IllegalArgumentException("Valid for basic annotations only.");

            final var toString = Object.class.getMethod("toString");
            final var annotationType = Annotation.class.getMethod("annotationType");
            final var equals = Object.class.getMethod("equals", Object.class);
            final var hashCode = Object.class.getMethod("hashCode");

            final InvocationHandler ih = (proxy, method, args) -> {
                if (method.equals(toString)) {
                    return "@" + type.getName() + "()";
                } else if (method.equals(annotationType)) {
                    return type;
                } else if (method.equals(equals)) {
                    return type.isInstance(args[0]);
                } else if (method.equals(hashCode)) {
                    return 0;
                } else {
                    throw new UnsupportedOperationException("Not supported.");
                }
            };

            final var interfaces = new Class<?>[]{type};
            final var cl = DefaultBindingAnnotationFactory.class.getClassLoader();

            return (Annotation) Proxy.newProxyInstance(cl, interfaces, ih);

        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException(e);
        }

    }

}

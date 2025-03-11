package dev.getelements.elements.sdk.query;

import dev.getelements.elements.sdk.Callback;
import dev.getelements.elements.sdk.exception.SdkCallbackException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A simple {@link Callback} for a specific {@link Method}.
 *
 * @param service the service
 * @param method the method
 * @param <ServiceT>
 */
public record MethodCallbackRecord<ServiceT>(ServiceT service, Method method) implements Callback<Object> {

    public MethodCallbackRecord {
        if (service == null && !Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Service is null for non-static method:" + method);
        } else if (service != null && Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Service is non-null for static method:" + method);
        }
    }

    @Override
    public Object call(final Object... args) {
        try {
            return method().invoke(service(), args);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new SdkCallbackException(ex);
        }
    }

    @Override
    public <NewResultT> Callback<NewResultT> as(final Class<NewResultT> newResultTClass) {
        if (newResultTClass.isAssignableFrom(method().getReturnType())) {
            return Callback.super.as(newResultTClass);
        } else {
            throw new SdkCallbackException(
                    "Incompatible return type: " + newResultTClass +
                    " not assignable from return type of " + method
            );
        }
    }

}

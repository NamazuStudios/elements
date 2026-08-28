package dev.getelements.elements.sdk.cluster.remote.proxy;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * Identifies a specific {@link Method} invoked on a specific proxy instance of a specific interface, used as a
 * lookup key for caching the resulting {@link java.lang.invoke.MethodHandle}.
 *
 * @param interfaceClassT the proxied interface {@link Class}
 * @param proxy the proxy instance on which the method was invoked
 * @param method the {@link Method} being invoked
 */
public record MethodHandleKey(Class<?> interfaceClassT, Object proxy, Method method) {

    /**
     * Derives the {@link MethodType} for {@link #method()} from its return type and parameter types.
     *
     * @return the {@link MethodType}
     */
    public MethodType getMethodType() {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes());
    }

}

package dev.getelements.elements.sdk.cluster.remote.proxy;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

record MethodHandleKey(Class<?> interfaceClassT, Object proxy, Method method) {

    public MethodType getMethodType() {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes());
    }

}

package dev.getelements.elements.rt.remote;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

class MethodHandleKey {

    private final Class<?> interfaceClassT;

    private final Object proxy;

    private final Method method;

    public MethodHandleKey(final Class<?> interfaceClassT, final Object proxy, final Method method) {
        this.interfaceClassT = interfaceClassT;
        this.proxy = proxy;
        this.method = method;
    }

    public Class<?> getInterfaceClassT() {
        return interfaceClassT;
    }

    public Object getProxy() {
        return proxy;
    }

    public Method getMethod() {
        return method;
    }

    public MethodType getMethodType() {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodHandleKey)) return false;

        MethodHandleKey that = (MethodHandleKey) o;

        if (!getInterfaceClassT().equals(that.getInterfaceClassT())) return false;
        if (!getProxy().equals(that.getProxy())) return false;
        return getMethod().equals(that.getMethod());
    }

    @Override
    public int hashCode() {
        int result = getInterfaceClassT().hashCode();
        result = 31 * result + getProxy().hashCode();
        result = 31 * result + getMethod().hashCode();
        return result;
    }

}

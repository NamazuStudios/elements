package dev.getelements.elements.sdk.cluster.remote;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface ProxyBuilder<ProxyT> {

    ProxyBuilder<ProxyT> dontProxyDefaultMethods();

    ProxyBuilder<ProxyT> withSharedMethodHandleCache();

    ProxyBuilder<ProxyT> withMethodHandleCache(BiFunction<MethodHandleKey, Supplier<MethodHandle>, MethodHandle> methodHandleCache);

    MethodAssignment<ProxyBuilder<ProxyT>> handler(InvocationHandler invocationHandler);

    ProxyBuilder<ProxyT> withDefaultHandler(InvocationHandler defaultInvocationHandler);

    ProxyBuilder<ProxyT> withToString();

    ProxyBuilder<ProxyT> withToString(String toString);

    ProxyBuilder<ProxyT> withDefaultHashCodeAndEquals();

    ProxyBuilder<ProxyT> withHandlersForRemoteInvoker(RemoteInvoker remoteInvoker);

    ProxyBuilder<ProxyT> withHandlersForRemoteDispatcher(RemoteInvocationDispatcher remoteInvocationDispatcher);

    ProxyT build();

}

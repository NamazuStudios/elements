package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Builds an instance of {@link InvocationHandler} based on the underlying {@link Method} and {@link RemoteInvoker}.
 */
public class RemoteInvocationHandlerBuilder {

    private final Method method;

    private final RemoteInvoker remoteInvoker;

    public RemoteInvocationHandlerBuilder(final RemoteInvoker remoteInvoker, final Method method) {
        this.method = method;
        this.remoteInvoker = remoteInvoker;
    }

    public Method getMethod() {
        return method;
    }

    public InvocationHandler build() {

        final Function<Object[], List<Object>> parameterAssembler = getParameterAssembler(method);

        return (proxy, method1, args) -> {
            final Invocation invocation = new Invocation();
            invocation.setParameters(parameterAssembler.apply(args));
            return null;
        };

    }

    private Function<Object[], List<Object>> getParameterAssembler(final Method method) {
        return objects -> {
            return Collections.emptyList();
        };
    }

}

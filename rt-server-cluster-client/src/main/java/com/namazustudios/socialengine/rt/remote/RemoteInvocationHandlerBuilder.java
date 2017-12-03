package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RemoteInvocationHandlerBuilder {

    private final RemoteInvoker remoteInvoker;

    public RemoteInvocationHandlerBuilder(RemoteInvoker remoteInvoker) {
        this.remoteInvoker = remoteInvoker;
    }

    public RemoteInvocationHandlerBuilder forMethod(final Method method) {
        return this;
    }

    public InvocationHandler build() {
        return null;
    }

}

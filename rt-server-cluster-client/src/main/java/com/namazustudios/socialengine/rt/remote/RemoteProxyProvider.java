package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.rt.Reflection.methods;

public class RemoteProxyProvider<ProxyableT> implements Provider<ProxyableT> {

    private Provider<RemoteInvoker> remoteInvokerProvider;

    private final String name;

    private final Class<ProxyableT> proxyableTClass;

    public RemoteProxyProvider(final Class<ProxyableT> proxyableTClass, final String name) {
        this.name = name;
        this.proxyableTClass = proxyableTClass;
    }

    @Override
    public ProxyableT get() {

        final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();
        final ProxyBuilder<ProxyableT> builder = new ProxyBuilder<>(proxyableTClass).dontProxyDefaultMethods();

        methods(proxyableTClass)
            .filter(m -> m.getAnnotation(RemotelyInvokable.class) != null)
            .map(m -> new RemoteInvocationHandlerBuilder(remoteInvoker, proxyableTClass, m).withName(name))
            .forEach(b -> builder.handler(b.build()).forMethod(b.getMethod()));

        return builder.build();

    }

    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
        return remoteInvokerProvider;
    }

    @Inject
    public void setRemoteInvokerProvider(Provider<RemoteInvoker> remoteInvokerProvider) {
        this.remoteInvokerProvider = remoteInvokerProvider;
    }

}

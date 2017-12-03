package com.namazustudios.socialengine.rt.remote;

import javax.inject.Inject;
import javax.inject.Provider;

public class RemoteProxyProvider<ProxyableT> implements Provider<ProxyableT> {

    private Provider<RemoteInvoker> remoteInvokerProvider;

    private final Class<ProxyableT> proxyableTClass;

    public RemoteProxyProvider(final Class<ProxyableT> proxyableTClass) {
        this.proxyableTClass = proxyableTClass;
    }

    @Override
    public ProxyableT get() {

        final RemoteInvoker remoteInvoker = getRemoteInvokerProvider().get();

        final ProxyBuilder<ProxyableT> builder = new ProxyBuilder<>(proxyableTClass)
            .dontProxyDefaultMethods()
            .withDefaultHandler(((proxy, method, args) -> {
                return null;
            }));

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

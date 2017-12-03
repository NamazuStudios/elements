package com.namazustudios.socialengine.rt.remote;

import javax.inject.Provider;

public class RemoteProxyProvider<ProxyableT> implements Provider<ProxyableT> {

    private final ProxyBuilder<ProxyableT> proxyBuilder;

    public RemoteProxyProvider(ProxyBuilder<ProxyableT> proxyBuilder) {
        this.proxyBuilder = proxyBuilder;
    }

    @Override
    public ProxyableT get() {
        return proxyBuilder.build();
    }

}

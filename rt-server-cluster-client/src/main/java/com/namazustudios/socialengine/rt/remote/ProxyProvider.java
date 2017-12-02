package com.namazustudios.socialengine.rt.remote;

import javax.inject.Provider;

public class ProxyProvider<ProxyableT> implements Provider<ProxyableT> {

    private final ProxyBuilder<ProxyableT> proxyBuilder;

    public ProxyProvider(ProxyBuilder<ProxyableT> proxyBuilder) {
        this.proxyBuilder = proxyBuilder;
    }

    @Override
    public ProxyableT get() {
        return proxyBuilder.build();
    }

}

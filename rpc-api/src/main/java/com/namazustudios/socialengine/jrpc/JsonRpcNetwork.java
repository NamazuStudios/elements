package com.namazustudios.socialengine.jrpc;

import com.namazustudios.socialengine.jrpc.provider.BscRpcUrlProvider;
import com.namazustudios.socialengine.jrpc.provider.BscTestRpcUrlProvider;

import javax.inject.Provider;

public enum JsonRpcNetwork {

    /**
     * Binance Smart Chain.
     */
    BSC("bsc", "bsc", BscRpcUrlProvider.class),

    /**
     *
     */
    BSC_TEST("bsc", "sc_test", BscTestRpcUrlProvider.class);

    private final String scope;

    private final String prefix;

    private final Class<? extends Provider<String>> urlProvider;

    JsonRpcNetwork(final String scope, final String prefix, final Class<? extends Provider<String>> urlProvider) {
        this.scope = scope;
        this.prefix = prefix;
        this.urlProvider = urlProvider;
    }

    /**
     * Gets the scope of services associated with the network.
     *
     * @return the scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * Gets the prefix for the endpoint.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the URL provider type.
     *
     * @return the URL provider type
     */
    public Class<? extends Provider<String>> getUrlProvider() {
        return urlProvider;
    }

}

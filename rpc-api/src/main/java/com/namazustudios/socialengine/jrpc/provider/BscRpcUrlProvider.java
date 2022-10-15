package com.namazustudios.socialengine.jrpc.provider;

import java.util.List;

public class BscRpcUrlProvider extends AbstractRandomUrlProvider {
    public BscRpcUrlProvider() {
        urls = List.of(
            "https://bsc-dataseed.binance.org/",
            "https://bsc-dataseed1.defibit.io/",
            "https://bsc-dataseed1.ninicoin.io/",
            "https://bsc.nodereal.io"
        );
    }
}

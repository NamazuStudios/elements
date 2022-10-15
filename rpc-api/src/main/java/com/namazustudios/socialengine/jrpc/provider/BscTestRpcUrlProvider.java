package com.namazustudios.socialengine.jrpc.provider;

import java.util.List;

public class BscTestRpcUrlProvider extends AbstractRandomUrlProvider {
    public BscTestRpcUrlProvider() {
        urls = List.of(
            "https://data-seed-prebsc-1-s1.binance.org:8545/",
            "https://data-seed-prebsc-2-s1.binance.org:8545/",
            "https://data-seed-prebsc-1-s2.binance.org:8545/",
            "https://data-seed-prebsc-2-s2.binance.org:8545/",
            "https://data-seed-prebsc-1-s3.binance.org:8545/",
            "https://data-seed-prebsc-2-s3.binance.org:8545/"
        );
    }
}

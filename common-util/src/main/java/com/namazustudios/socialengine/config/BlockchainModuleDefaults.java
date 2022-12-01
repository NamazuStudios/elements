package com.namazustudios.socialengine.config;

import java.util.List;
import java.util.Properties;

import static com.namazustudios.socialengine.model.blockchain.BlockchainNetwork.*;

public class BlockchainModuleDefaults implements ModuleDefaults {


    @Override
    public Properties get() {
        final var properties = new Properties();

        // Neo Requiries running
        properties.put(NEO.urlsName(), "http://localhost:40332");
        properties.put(NEO_TEST.urlsName(), "http://localhost:40332");

        // Unless Specifically Configured. BSC Main still uses Test Net

        properties.put(BSC.urlsName(), String.join(",", List.of(
                "https://data-seed-prebsc-1-s1.binance.org:8545/",
                "https://data-seed-prebsc-2-s1.binance.org:8545/",
                "https://data-seed-prebsc-1-s2.binance.org:8545/",
                "https://data-seed-prebsc-2-s2.binance.org:8545/",
                "https://data-seed-prebsc-1-s3.binance.org:8545/",
                "https://data-seed-prebsc-2-s3.binance.org:8545/"
        )));

        properties.put(BSC_TEST.urlsName(), String.join(",", List.of(
                "https://data-seed-prebsc-1-s1.binance.org:8545/",
                "https://data-seed-prebsc-2-s1.binance.org:8545/",
                "https://data-seed-prebsc-1-s2.binance.org:8545/",
                "https://data-seed-prebsc-2-s2.binance.org:8545/",
                "https://data-seed-prebsc-1-s3.binance.org:8545/",
                "https://data-seed-prebsc-2-s3.binance.org:8545/"
        )));

        return properties;

    }

}

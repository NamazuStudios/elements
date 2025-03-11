package dev.getelements.elements.config;

import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;

import java.util.List;
import java.util.Properties;

import static dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork.*;

public class BlockchainModuleDefaults implements ModuleDefaults {


    @Override
    public Properties get() {
        final var properties = new Properties();

        properties.put(NEO.urlsName(), "http://localhost:40332");
        properties.put(NEO_TEST.urlsName(), "http://localhost:40332");

        properties.put(ETHEREUM.urlsName(), "127.0.0.1:8545");
        properties.put(ETHEREUM_TEST.urlsName(), "127.0.0.1:8545");

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

        properties.put(POLYGON.urlsName(), "https://polygon-rpc.com/");
        properties.put(POLYGON_TEST.urlsName(), "https://rpc-mumbai.matic.today/");

        properties.put(SOLANA.urlsName(), "http://localhost:8899/");
        properties.put(SOLANA_TEST.urlsName(), "http://localhost:8899/");

        properties.put(FLOW.urlsName(), "grpc://access.devnet.nodes.onflow.org:9000");
        properties.put(FLOW_TEST.urlsName(), "grpc://access.devnet.nodes.onflow.org:9000");

        //https://docs.near.org/api/rpc/setup
        properties.put(NEAR.urlsName(), "https://rpc.mainnet.near.org");
        properties.put(NEAR_TEST.urlsName(), "https://rpc.testnet.near.org");

        return properties;

    }

}

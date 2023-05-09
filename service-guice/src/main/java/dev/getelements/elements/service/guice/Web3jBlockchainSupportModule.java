package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;

import java.util.stream.Stream;

import static dev.getelements.elements.model.blockchain.BlockchainApi.ETHEREUM;

public class Web3jBlockchainSupportModule extends AbstractModule {

    @Override
    protected void configure() {

        Stream.of(BlockchainNetwork.values())
                .filter(network -> ETHEREUM.equals(network.api()))
                .forEach(network -> install(new Web3jNetworkModule(network)));

    }

}

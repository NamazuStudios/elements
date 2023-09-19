package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;

import java.util.stream.Stream;

import static dev.getelements.elements.model.blockchain.BlockchainApi.NEAR;

public class NearBlockchainSupportModule extends AbstractModule {

    @Override
    protected void configure() {

        Stream.of(BlockchainNetwork.values())
                .filter(network -> NEAR.equals(network.api()))
                .forEach(network -> install(new NearNetworkModule(network)));

    }

}


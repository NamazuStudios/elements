package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;

import java.util.stream.Stream;

import static dev.getelements.elements.sdk.model.blockchain.BlockchainApi.FLOW;

public class FlowBlockchainSupportModule extends AbstractModule {

    @Override
    protected void configure() {
        Stream.of(BlockchainNetwork.values())
                .filter(network -> FLOW.equals(network.api()))
                .forEach(network -> install(new FlowNetworkModule(network)));
    }

}

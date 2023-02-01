package com.namazustudios.socialengine.service.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;

import java.util.stream.Stream;

import static com.namazustudios.socialengine.model.blockchain.BlockchainApi.FLOW;

public class FlowBlockchainSupportModule extends AbstractModule {

    @Override
    protected void configure() {
        Stream.of(BlockchainNetwork.values())
                .filter(network -> FLOW.equals(network.api()))
                .forEach(network -> install(new FlowNetworkModule(network)));
    }

}

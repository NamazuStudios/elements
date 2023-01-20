package com.namazustudios.socialengine.service.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;

import java.util.stream.Stream;

import static com.namazustudios.socialengine.model.blockchain.BlockchainApi.ETHEREUM;

public class Web3jBlockchainSupportModule extends AbstractModule {

    @Override
    protected void configure() {
        Stream.of(BlockchainNetwork.values())
                .filter(network -> ETHEREUM.equals(network.api()))
                .forEach(network -> install(new Web3jNetworkModule(network)));
    }

}

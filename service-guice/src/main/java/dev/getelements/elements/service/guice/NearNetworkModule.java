package dev.getelements.elements.service.guice;

import com.google.inject.PrivateModule;
import com.syntifi.near.api.common.helper.Network;
import com.syntifi.near.api.rpc.NearClient;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.service.blockchain.NearSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.invoke.near.NearInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.google.inject.name.Names.named;

public class NearNetworkModule extends PrivateModule {

    private static final Logger logger = LoggerFactory.getLogger(NearNetworkModule.class);

    private final BlockchainNetwork network;

    NearNetworkModule(final BlockchainNetwork network) {

        if (!BlockchainApi.NEAR.equals(network.api())) {
            throw new IllegalArgumentException("Must be NEAR API Network.");
        }

        this.network = network;

    }

    @Override
    protected void configure() {

        bind(NearClient.class).toProvider(() -> {

            if(network == BlockchainNetwork.NEAR) {
                return NearClient.usingNetwork(Network.MAIN_NET);
            }

            if(network == BlockchainNetwork.NEAR_TEST) {
                return NearClient.usingNetwork(Network.TEST_NET);
            }

            throw new InternalException("Invalid network");

        }).asEagerSingleton();

        bind(NearSmartContractInvocationService.Invoker.class)
                .annotatedWith(named(network.iocName()))
                .to(NearInvoker.class);

        expose(NearSmartContractInvocationService.Invoker.class)
                .annotatedWith(named(network.iocName()));
    }

}

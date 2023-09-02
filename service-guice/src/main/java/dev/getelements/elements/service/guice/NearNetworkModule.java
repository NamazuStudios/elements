package dev.getelements.elements.service.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.syntifi.near.api.common.helper.Network;
import com.syntifi.near.api.rpc.NearClient;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.service.NearSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.invoke.near.NearInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

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

        final var urlKey = Key.get(String.class, named(network.urlsName()));
        final var urlProvider = getProvider(urlKey);

        bind(NearClient.class).toProvider(() -> {

            final URI uri;

            try {
                uri = new URI(urlProvider.get());
            } catch (URISyntaxException ex) {
                throw new InternalException(ex);
            }

            if (!"grpc".equals(uri.getScheme())) {
                logger.warn("Incorrect Flow scheme specification: Expected grpc but got {}.", uri.getScheme());
            }

            if(network == BlockchainNetwork.NEAR) {
                return NearClient.usingNetwork(Network.MAIN_NET);
            }

            return NearClient.usingNetwork(Network.TEST_NET);

        }).asEagerSingleton();

        bind(NearSmartContractInvocationService.Invoker.class)
                .annotatedWith(named(network.iocName()))
                .to(NearInvoker.class);

        expose(NearSmartContractInvocationService.Invoker.class)
                .annotatedWith(named(network.iocName()));
    }

}

package dev.getelements.elements.service.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.rt.util.Monitor;
import dev.getelements.elements.service.EvmSmartContractInvocationService;
import dev.getelements.elements.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.service.blockchain.invoke.evm.Web3jInvoker;
import dev.getelements.elements.util.RoundRobin;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import static com.google.inject.name.Names.named;

class Web3jNetworkModule extends PrivateModule {

    private final BlockchainNetwork network;

    Web3jNetworkModule(final BlockchainNetwork network) {

        if (!BlockchainApi.ETHEREUM.equals(network.api())) {
            throw new IllegalArgumentException("Must be ETHEREUM API Network.");
        }

        this.network = network;

    }

    @Override
    protected void configure() {

        bind(Web3j.class)
                .toProvider(new RoundRobinWeb3jProvider());

        bind(EvmSmartContractInvocationService.Invoker.class)
                .annotatedWith(named(network.iocName()))
                .to(Web3jInvoker.class);

        expose(EvmSmartContractInvocationService.Invoker.class)
                .annotatedWith(named(network.iocName()));

    }

    private class RoundRobinWeb3jProvider implements Provider<Web3j> {

        private RoundRobin<Web3j> web3jRoundRobin;

        private final Provider<String> urlsProvider;

        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        public RoundRobinWeb3jProvider() {
            final var key = Key.get(String.class, named(network.urlsName()));
            urlsProvider = getProvider(key);
        }

        @Override
        public Web3j get() {

            try (final var monitor = Monitor.enter(readWriteLock.readLock())) {
                if (web3jRoundRobin != null) {
                    return web3jRoundRobin.next();
                }
            }

            try (final var monitor = Monitor.enter(readWriteLock.writeLock())) {
                if (web3jRoundRobin != null) {
                    return web3jRoundRobin.next();
                } else {

                    final var web3jStream = Stream.of(urlsProvider.get().split(","))
                            .map(HttpService::new)
                            .map(Web3j::build);

                    web3jRoundRobin = new RoundRobin<>(web3jStream);
                    return web3jRoundRobin.next();

                }
            }

        }

    }

}

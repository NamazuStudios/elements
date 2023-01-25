package com.namazustudios.socialengine.service.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;

import java.util.stream.Stream;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.model.blockchain.BlockchainApi.ETHEREUM;
import static com.namazustudios.socialengine.service.EvmSmartContractInvocationService.IOC_NAME;

public class Web3jBlockchainSupportModule extends AbstractModule {

    @Override
    protected void configure() {

        final var guiceIocResolverKey = Key.get(
                GuiceIoCResolver.class,
                named(IOC_NAME)
        );

        final var membersInjector = getMembersInjector(GuiceIoCResolver.class);

        bind(GuiceIoCResolver.class)
                .annotatedWith(named(IOC_NAME))
                .toProvider(() -> {
                    final var resolver = new GuiceIoCResolver();
                    membersInjector.injectMembers(resolver);
                    return resolver;
                }).asEagerSingleton();

        bind(IocResolver.class)
                .annotatedWith(named(IOC_NAME))
                .to(guiceIocResolverKey);

        Stream.of(BlockchainNetwork.values())
                .filter(network -> ETHEREUM.equals(network.api()))
                .forEach(network -> install(new Web3jNetworkModule(network)));

    }

}

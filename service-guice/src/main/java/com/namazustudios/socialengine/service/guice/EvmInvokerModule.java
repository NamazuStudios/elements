package com.namazustudios.socialengine.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.service.EvmSmartContractInvocationService.Invoker;
import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;
import com.namazustudios.socialengine.service.blockchain.invoke.evm.EvmInvocationScope;

import static com.google.inject.name.Names.named;

public class EvmInvokerModule extends AbstractModule {

    @Override
    protected void configure() {

        final var injectorProvider = getProvider(Injector.class);

        bind(new TypeLiteral<ScopedInvoker.Factory<EvmInvocationScope, Invoker>>(){}).toInstance(network -> {
            final var key = Key.get(Invoker.class, named(network.iocName()));
            return injectorProvider.get().getInstance(key);
        });

    }

}

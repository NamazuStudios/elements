package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.sdk.service.blockchain.FlowSmartContractInvocationService.Invoker;
import dev.getelements.elements.sdk.service.blockchain.invoke.ScopedInvoker.Factory;
import dev.getelements.elements.sdk.service.blockchain.invoke.flow.FlowInvocationScope;

import static com.google.inject.name.Names.named;

public class FlowInvokerModule extends AbstractModule {

    @Override
    protected void configure() {

        final var injectorProvider = getProvider(Injector.class);

        bind(new TypeLiteral<Factory<FlowInvocationScope, Invoker>>(){}).toInstance(network -> {
            final var key = Key.get(Invoker.class, named(network.iocName()));
            return injectorProvider.get().getInstance(key);
        });

    }

}

package dev.getelements.elements.sdk.service.blockchain.invoke;

import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.service.blockchain.EvmSmartContractInvocationService.Invoker;

/**
 * Internal interface used to handle scoping of {@link Invoker} instances.
 */
public interface ScopedInvoker<InvocationScopeT extends InvocationScope> {

    /**
     * Initializes the {@link Invoker} with the invocation scope.
     *
     * @param invocationScope the {@link InvocationScope}
     */
    void initialize(InvocationScopeT invocationScope);

    /**
     * A Factory type which is used to make instances of {@link }
     * @param <InvocationScopeT>
     */
    interface Factory<InvocationScopeT extends InvocationScope, InvokerT extends ScopedInvoker<InvocationScopeT>> {

        /**
         * Creates a new {@link ScopedInvoker}.
         *
         * @param network the network
         * @return a new {@link ScopedInvoker}
         * @throws IllegalArgumentException if the supplied network does not have an invoker.
         */
        InvokerT create(BlockchainNetwork network);

    }

}

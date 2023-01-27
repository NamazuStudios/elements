package com.namazustudios.socialengine.service.blockchain.invoke;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.service.EvmSmartContractInvocationService.Invoker;
import com.namazustudios.socialengine.service.blockchain.invoke.evm.EvmInvocationScope;

/**
 * Internal interface used to handle scoping of {@link Invoker} instances.
 */
public interface ScopedInvoker<InvocationScopeT extends InvocationScope> {

    /**
     * Initializes the {@link Invoker} with the invocation scope.
     *
     * @param evmInvocationScope the {@link EvmInvocationScope}
     */
    void initialize(InvocationScopeT evmInvocationScope);

    /**
     * A Factory type which is used to make instances of {@Link }
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

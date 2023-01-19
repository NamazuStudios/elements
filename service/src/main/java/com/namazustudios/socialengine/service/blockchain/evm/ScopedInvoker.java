package com.namazustudios.socialengine.service.blockchain.evm;

import com.namazustudios.socialengine.service.EvmSmartContractService.Invoker;

/**
 * Internal interface used to handle scoping of {@link Invoker} instances.
 */
public interface ScopedInvoker extends Invoker {

    /**
     * Initializes the {@link Invoker} with the invocation scope.
     *
     * @param evmInvocationScope the {@link EvmInvocationScope}
     */
    void initialize(EvmInvocationScope evmInvocationScope);

}

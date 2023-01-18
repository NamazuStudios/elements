package com.namazustudios.socialengine.service.blockchain.evm;

import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContractAddress;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.service.EvmSmartContractService.Invoker;

/**
 * Used internally to resolve instances of {@link Invoker}.
 */
public interface InvokerFactory {

    /**
     * Creates a new {@link Invoker}
     *
     * @param vault the {@link Vault} used to perform the invocation
     * @param smartContract the {@link SmartContract} t
     * @param smartContractAddress the {@link SmartContractAddress}
     * @return the {@link Invoker}
     */
    Invoker newInvoker(Vault vault, SmartContract smartContract, SmartContractAddress smartContractAddress);

}

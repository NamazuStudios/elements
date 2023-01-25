package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.blockchain.VaultNotFoundException;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;

/**
 * Returned by methods such as {@link EvmSmartContractInvocationService#resolve(String, BlockchainNetwork)} which
 * ensures that the underlying contract and configuration exists for the associated contract.
 */
public interface SmartContractInvocationResolution<InvokerT> {

    /**
     * Opens the {@link Vault} associated with the underlying {@link SmartContract}.
     *
     * @return the {@link EvmSmartContractInvocationService.Invoker} used to invoke the smart contract methods
     * @throws IllegalStateException if the vault is locked
     */
    InvokerT open();

    /**
     * Unlocks the underlying {@link Vault} with the supplied passphrase.
     *
     * @param passphrase the passphrase
     * @return the {@link EvmSmartContractInvocationService.Invoker} used to invoke the smart contract methods
     */
    EvmSmartContractInvocationService.Invoker unlock(String passphrase);

    /**
     * Creates a new {@link SmartContractInvocationResolution} with a {@link Vault} not necessarily with the underlying
     * {@link SmartContract}. This allows code to swap signing keys just in time.
     *
     * @param vaultId the vault id
     * @return a new {@link SmartContractInvocationResolution} associated with the new vault
     * @throws VaultNotFoundException if there is no vault with the supplied id
     */
    SmartContractInvocationResolution vault(String vaultId);

}

package dev.getelements.elements.sdk.service.blockchain;

import dev.getelements.elements.sdk.model.exception.blockchain.VaultNotFoundException;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContract;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;

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
    InvokerT unlock(String passphrase);

    /**
     * Creates a new {@link SmartContractInvocationResolution} with a {@link Vault} not necessarily with the underlying
     * {@link SmartContract}. This allows code to swap signing keys just in time.
     *
     * @param vaultId the vault id
     * @return a new {@link SmartContractInvocationResolution} associated with the new vault
     * @throws VaultNotFoundException if there is no vault with the supplied id
     */
    SmartContractInvocationResolution<InvokerT> vault(String vaultId);

}

package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.blockchain.VaultNotFoundException;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.EVMInvokeContractResponse;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.math.BigInteger;
import java.util.List;

@Expose({
        @ModuleDefinition(
                value = "namazu.elements.service.smartcontract.evm"),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.smartcontract.evm",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
/**
 * A service which allows for the invocation of methods belonging to an EVM Smart Contract. Several blockchain networks
 * are EVM based even if the API is not ETH based. Therefore this service can handle the invocation details for the
 * EVM based networks.
 */
public interface EvmSmartContractService {

    /**
     * Gas Price
     *
     * @deprecated migrate this to a configurable parameter
     */
   BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(20000000000L);

    /**
     * Gas Limits
     *
     * @deprecated migrate this to a configurable parameter
     */
    BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(6721975);

    /**
     * Generates a {@link Resolution} for the supplied contract, which can be used to invoke the underlying smart
     * contract methods.
     *
     * @param contractNameOrId the contract name or ID
     * @param network the network
     * @return the {@link Resolution}
     */
    Resolution resolve(String contractNameOrId, BlockchainNetwork network);

    /**
     * {@see {@link EvmSmartContractService#resolve(String, String)}}. Used to support scripting components.
     *
     * @param contractNameOrId the contract name or ID
     * @param network the network
     * @return the {@link Resolution}
     */
    default Resolution resolve(final String contractNameOrId, final String network) {
        return resolve(contractNameOrId, BlockchainNetwork.valueOf(network));
    }

    /**
     * A type which performs the final invocation to the underlying smart contract.
     */
    interface Invoker {

        /**
         * Calls the smart contract method with the supplied parameter types and return types.
         *
         * @param method the method name
         * @param inputTypes the parameter types
         * @param arguments the parameters themselves
         * @param outputTypes the return types
         * @return the return result of the call
         */
        Object call(
                String method,
                List<String> inputTypes,
                List<Object> arguments,
                List<String> outputTypes);

        /**
         * Sends a transaction to the blockchain by calling the specified smart contract method with the supplied
         * parameter types and return types.
         *
         * @param method the method name
         * @param inputTypes the parameter types
         * @param arguments the parameters themselves
         * @param outputTypes the return types
         * @return the return result of the call
         */
        EVMInvokeContractResponse send(
                String network,
                String method,
                List<String> inputTypes,
                List<Object> arguments,
                List<String> outputTypes);

    }

    /**
     * Returned by the {@link EvmSmartContractService#resolve(String, BlockchainNetwork)} (and related methods) which
     * ensures that the underlying contract and configuration exists for the associated contract.
     */
    interface Resolution {

        /**
         * Opens the {@link Vault} associated with the underlying {@link SmartContract}.
         *
         * @return the {@link Invoker} used to invoke the smart contract methods
         * @throws IllegalStateException if the vault is locked
         */
        Invoker open();

        /**
         * Unlocks the underlying {@link Vault} with the supplied passphrase.
         *
         * @param passphrase the passphrase
         * @return the {@link Invoker} used to invoke the smart contract methods
         */
        Invoker unlock(String passphrase);

        /**
         * Creates a new {@link Resolution} with a {@link Vault} not necessarily with the underlying
         * {@link SmartContract}. This allows code to swap signing keys just in time.
         *
         * @param vaultId the vault id
         * @return a new {@link Resolution} associated with the new vault
         * @throws VaultNotFoundException if there is no vault with the supplied id
         */
        Resolution vault(String vaultId);

    }

}

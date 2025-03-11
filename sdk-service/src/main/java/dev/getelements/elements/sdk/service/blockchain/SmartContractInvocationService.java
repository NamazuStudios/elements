package dev.getelements.elements.sdk.service.blockchain;

import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;

public interface SmartContractInvocationService<InvokerT> {

    /**
     * Generates a {@link SmartContractInvocationResolution} for the supplied contract, which can be used to invoke the underlying smart
     * contract methods.
     *
     * @param contractNameOrId the contract name or ID
     * @param network the network
     * @return the {@link SmartContractInvocationResolution}
     */
    SmartContractInvocationResolution<InvokerT> resolve(String contractNameOrId, BlockchainNetwork network);

    /**
     * {@see {@link EvmSmartContractInvocationService#resolve(String, String)}}. Used to support scripting components.
     *
     * @param contractNameOrId the contract name or ID
     * @param network the network
     * @return the {@link SmartContractInvocationResolution}
     */
    default SmartContractInvocationResolution<InvokerT> resolve(final String contractNameOrId, final String network) {
        return resolve(contractNameOrId, BlockchainNetwork.valueOf(network));
    }


}

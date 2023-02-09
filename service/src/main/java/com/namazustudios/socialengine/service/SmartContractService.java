package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.PatchSmartContractRequest;
import com.namazustudios.socialengine.model.blockchain.contract.CreateSmartContractRequest;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.UpdateSmartContractRequest;

import java.util.List;

public interface SmartContractService {
    /**
     * Lists all {@link SmartContract} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link SmartContract} instances
     */
    Pagination<SmartContract> getSmartContracts(
            int offset,
            int count,
            BlockchainApi blockchainApi,
            List<BlockchainNetwork> blockchainNetworks);

    /**
     * Fetches a specific {@link SmartContract} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param contractId the contract ID
     * @return the {@link SmartContract}, never null
     */
    SmartContract getSmartContract(String contractId);

    /**
     * Creates a new smart contract.
     *
     * @param createSmartContractRequest creates a smart contract
     * @return the {@link SmartContract}
     */
    SmartContract createSmartContract(CreateSmartContractRequest createSmartContractRequest);

    /**
     * Updates the supplied {@link SmartContract}.  The
     * {@link PatchSmartContractRequest} method is used to key the
     * {@link SmartContract}.
     *
     * @param updateSmartContractRequest the {@link UpdateSmartContractRequest} with the information to update
     * @return the {@link SmartContract} as it was changed by the service.
     */
    SmartContract updateSmartContract(String contractId, UpdateSmartContractRequest updateSmartContractRequest);

    /**
     * Deletes the {@link SmartContract} with the supplied contract ID.
     *
     * @param contractId the contract ID.
     */
    void deleteContract(String contractId);

}

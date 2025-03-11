package dev.getelements.elements.sdk.service.blockchain;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.PatchSmartContractRequest;
import dev.getelements.elements.sdk.model.blockchain.contract.CreateSmartContractRequest;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContract;
import dev.getelements.elements.sdk.model.blockchain.contract.UpdateSmartContractRequest;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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

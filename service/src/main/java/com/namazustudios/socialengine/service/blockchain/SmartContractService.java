package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.PatchSmartContractRequest;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.UpdateSmartContractRequest;

public interface SmartContractService {
    /**
     * Lists all {@link SmartContract} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param search
     * @return a {@link Pagination} of {@link SmartContract} instances
     */
    Pagination<SmartContract> getSmartContracts(int offset, int count, String search);

    /**
     * Fetches a specific {@link SmartContract} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param contractId the contract ID
     * @return the {@link SmartContract}, never null
     */
    SmartContract getSmartContract(String contractId);

    /**
     * Updates the supplied {@link SmartContract}.  The
     * {@link PatchSmartContractRequest} method is used to key the
     * {@link SmartContract}.
     *
     * @param patchSmartContractRequest the {@link PatchSmartContractRequest} with the information to update
     * @return the {@link SmartContract} as it was changed by the service.
     */
    SmartContract updateSmartContract(UpdateSmartContractRequest patchSmartContractRequest);

    /**
     * Deletes the {@link SmartContract} with the supplied contract ID.
     *
     * @param contractId the contract ID.
     */
    void deleteContract(String contractId);

}

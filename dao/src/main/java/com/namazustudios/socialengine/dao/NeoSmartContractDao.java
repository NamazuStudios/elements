package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Created by patricktwohig on 6/28/17.
 */
@Expose({
        @ExposedModuleDefinition("namazu.elements.dao.neosmartcontract"),
        @ExposedModuleDefinition(
                value = "namazu.socialengine.dao.neosmartcontract",
                deprecated = @DeprecationDefinition("Use namazu.elements.dao.neosmartcontract instead"))
})
public interface NeoSmartContractDao {

    /**
     * Lists all {@link SmartContract} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param search
     * @return a {@link Pagination} of {@link SmartContract} instances
     */
    Pagination<SmartContract> getNeoSmartContracts(int offset, int count, String search);

    /**
     * Fetches a specific {@link SmartContract} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param contractIdOrName the contract ID
     * @return the {@link SmartContract}, never null
     */
    SmartContract getNeoSmartContract(String contractIdOrName);

    /**
     * Updates the supplied {@link SmartContract}.
     *
     * @param patchSmartContractRequest the {@link PatchSmartContractRequest} with the information to update
     * @return the {@link SmartContract} as it was changed by the service.
     */
    SmartContract patchNeoSmartContract(PatchSmartContractRequest patchSmartContractRequest);

    /**
     * Deletes the {@link SmartContract} with the supplied contract ID.
     *
     * @param contractId the contract ID.
     */
    void deleteNeoSmartContract(String contractId);

}

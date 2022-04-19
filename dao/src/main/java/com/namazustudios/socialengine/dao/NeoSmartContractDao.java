package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

/**
 * Created by patricktwohig on 6/28/17.
 */
@Expose({
    @ModuleDefinition("namazu.elements.dao.neo.smartcontract"),
    @ModuleDefinition(
            value = "namazu.socialengine.dao.neo.smartcontract",
            deprecated = @DeprecationDefinition("Use namazu.elements.dao.neo.smartcontract instead")
    )
})
public interface NeoSmartContractDao {

    /**
     * Lists all {@link ElementsSmartContract} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param search
     * @return a {@link Pagination} of {@link ElementsSmartContract} instances
     */
    Pagination<ElementsSmartContract> getNeoSmartContracts(int offset, int count, String search);

    /**
     * Fetches a specific {@link ElementsSmartContract} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param contractIdOrName the contract ID
     * @return the {@link ElementsSmartContract}, never null
     */
    ElementsSmartContract getNeoSmartContract(String contractIdOrName);

    /**
     * Updates the supplied {@link ElementsSmartContract}.
     *
     * @param patchSmartContractRequest the {@link PatchSmartContractRequest} with the information to update
     * @return the {@link ElementsSmartContract} as it was changed by the service.
     */
    ElementsSmartContract patchNeoSmartContract(PatchSmartContractRequest patchSmartContractRequest);

    /**
     * Deletes the {@link ElementsSmartContract} with the supplied contract ID.
     *
     * @param contractId the contract ID.
     */
    void deleteNeoSmartContract(String contractId);

}

package dev.getelements.elements.dao;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.ElementsSmartContract;
import dev.getelements.elements.model.blockchain.PatchSmartContractRequest;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Created by patricktwohig on 6/28/17.
 */
@Expose({
        @ModuleDefinition("eci.elements.dao.bsc.smartcontract"),
        @ModuleDefinition(
                value = "namazu.elements.dao.bsc.smartcontract",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.bsc.smartcontract instead")
        ),
        @ModuleDefinition(
                value = "namazu.socialengine.dao.bsc.smartcontract",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.bsc.smartcontract instead")
        )
})
public interface BscSmartContractDao {

    /**
     * Lists all {@link ElementsSmartContract} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param search
     * @return a {@link Pagination} of {@link ElementsSmartContract} instances
     */
    Pagination<ElementsSmartContract> getBscSmartContracts(int offset, int count, String search);

    /**
     * Fetches a specific {@link ElementsSmartContract} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param contractIdOrName the contract ID
     * @return the {@link ElementsSmartContract}, never null
     */
    ElementsSmartContract getBscSmartContract(String contractIdOrName);

    /**
     * Updates the supplied {@link ElementsSmartContract}.
     *
     * @param patchSmartContractRequest the {@link PatchSmartContractRequest} with the information to update
     * @return the {@link ElementsSmartContract} as it was changed by the service.
     */
    ElementsSmartContract patchBscSmartContract(PatchSmartContractRequest patchSmartContractRequest);

    /**
     * Deletes the {@link ElementsSmartContract} with the supplied contract ID.
     *
     * @param contractId the contract ID.
     */
    void deleteBscSmartContract(String contractId);

}

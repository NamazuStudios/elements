package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.NeoSmartContract;
import com.namazustudios.socialengine.model.blockchain.PatchNeoSmartContractRequest;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

/**
 * Manages instances of {@link NeoSmartContract}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ExposedModuleDefinition(value = "namazu.elements.service.blockchain.neosmartcontract"),
        @ExposedModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.neosmartcontract",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface NeoSmartContractService {

        /**
         * Lists all {@link NeoSmartContract} instances, specifying a search query.
         *
         * @param offset
         * @param count
         * @param search
         * @return a {@link Pagination} of {@link NeoSmartContract} instances
         */
        Pagination<NeoSmartContract> getNeoSmartContracts(int offset, int count, String search);

        /**
         * Fetches a specific {@link NeoSmartContract} instance based on ID. If not found, an
         * exception is raised.
         *
         * @param contractId the contract ID
         * @return the {@link NeoSmartContract}, never null
         */
        NeoSmartContract getNeoSmartContract(String contractId);

        /**
         * Updates the supplied {@link NeoSmartContract}.  The
         * {@link PatchNeoSmartContractRequest} method is used to key the
         * {@link NeoSmartContract}.
         *
         * @param patchNeoSmartContractRequest the {@link PatchNeoSmartContractRequest} with the information to update
         * @return the {@link NeoSmartContract} as it was changed by the service.
         */
        NeoSmartContract patchNeoSmartContract(PatchNeoSmartContractRequest patchNeoSmartContractRequest);

        /**
         * Deletes the {@link NeoSmartContract} with the supplied contract ID.
         *
         * @param contractId the contract ID.
         */
        void deleteTemplate(String contractId);

}

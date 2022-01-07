package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;

/**
 * Manages instances of {@link SmartContract}.
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
         * Lists all {@link SmartContract} instances, specifying a search query.
         *
         * @param offset
         * @param count
         * @param search
         * @return a {@link Pagination} of {@link SmartContract} instances
         */
        Pagination<SmartContract> getNeoSmartContracts(int offset, int count, String search);

        /**
         * Fetches a specific {@link SmartContract} instance based on ID. If not found, an
         * exception is raised.
         *
         * @param contractId the contract ID
         * @return the {@link SmartContract}, never null
         */
        SmartContract getNeoSmartContract(String contractId);

        /**
         * Updates the supplied {@link SmartContract}.  The
         * {@link PatchSmartContractRequest} method is used to key the
         * {@link SmartContract}.
         *
         * @param patchSmartContractRequest the {@link PatchSmartContractRequest} with the information to update
         * @return the {@link SmartContract} as it was changed by the service.
         */
        SmartContract patchNeoSmartContract(PatchSmartContractRequest patchSmartContractRequest);

        /**
         * Mints the token id's supplied in the {@link MintTokenRequest} using their linked {@link SmartContract}.
         *
         * @param mintTokenRequest the {@link MintTokenRequest} containing the token id's and wallet with funds to mint.
         * @return the {@link NeoSendRawTransaction} response from the blockchain.
         */
        NeoSendRawTransaction mintToken(MintTokenRequest mintTokenRequest);

        /**
         * Invokes a method on the {@link SmartContract} corresponding to the passed contract id.
         *
         * @param invokeRequest the {@link InvokeContractRequest} with the information to invoke
         * @return the {@link NeoSendRawTransaction} response from the blockchain invocation.
         */
        NeoSendRawTransaction invoke(InvokeContractRequest invokeRequest);

        /**
         * Invokes a method on the {@link SmartContract} corresponding to the passed contract id.
         *
         * @param invokeRequest the {@link InvokeContractRequest} with the information to invoke
         * @return the {@link NeoSendRawTransaction} response from the blockchain invocation.
         */
        NeoInvokeFunction testInvoke(InvokeContractRequest invokeRequest);

        /**
         * Deletes the {@link SmartContract} with the supplied contract ID.
         *
         * @param contractId the contract ID.
         */
        void deleteContract(String contractId);

}

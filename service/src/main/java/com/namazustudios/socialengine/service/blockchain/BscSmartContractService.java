package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.model.blockchain.bsc.MintBscTokenResponse;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Manages instances of {@link ElementsSmartContract}.
 * <p>
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ModuleDefinition(value = "namazu.elements.service.blockchain.bsc.smartcontract"),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.bsc.unscoped.smartcontract",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface BscSmartContractService {

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
     * Fetches a specific {@link ElementsSmartContract} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param contractId the contract ID
     * @return the {@link ElementsSmartContract}, never null
     */
    ElementsSmartContract getBscSmartContract(String contractId);

    /**
     * Updates the supplied {@link ElementsSmartContract}.  The
     * {@link PatchSmartContractRequest} method is used to key the
     * {@link ElementsSmartContract}.
     *
     * @param patchSmartContractRequest the {@link PatchSmartContractRequest} with the information to update
     * @return the {@link ElementsSmartContract} as it was changed by the service.
     */
    ElementsSmartContract patchBscSmartContract(PatchSmartContractRequest patchSmartContractRequest);

    /**
     * Mints the token id's supplied in the {@link MintTokenRequest} using their linked {@link ElementsSmartContract}.
     *
     * @param mintTokenRequest  the {@link MintTokenRequest} containing the token id's and wallet with funds to mint.
     * @param exceptionConsumer
     * @return the {@link List<String>} responses from the blockchain.
     */
    String mintToken(final MintTokenRequest mintTokenRequest,
                               final Consumer<MintBscTokenResponse> applicationLogConsumer,
                               final Consumer<Throwable> exceptionConsumer);

    /**
     * Invokes a method on the {@link ElementsSmartContract} corresponding to the passed contract id
     * in a transactional manner. This will always incur a GAS fee.
     *
     * @param invokeRequest     the {@link InvokeContractRequest} with the information to invoke
     * @param exceptionConsumer
     * @return the {@link String} response from the blockchain invocation.
     */
    String  invoke(final InvokeContractRequest invokeRequest,
                            final BiConsumer<Long, InvokeContractResponse> applicationLogConsumer,
                            final Consumer<Throwable> exceptionConsumer);


    /**
     * Deletes the {@link ElementsSmartContract} with the supplied contract ID.
     *
     * @param contractId the contract ID.
     */
    void deleteContract(String contractId);

    /**
     * Represents a pending operation.
     */
    interface PendingOperation extends AutoCloseable {

        /**
         * Called to indicate the operation should be closed.
         */
        @Override
        void close();

    }

}

package dev.getelements.elements.service.blockchain.neo;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.*;
import dev.getelements.elements.model.blockchain.neo.MintNeoTokenResponse;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.Unscoped;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Manages instances of {@link ElementsSmartContract}.
 * <p>
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.blockchain.neo.smartcontract"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.blockchain.unscoped.neo.smartcontract",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.neo.smartcontract",
                deprecated = @DeprecationDefinition("Use eci.elements.service.blockchain.neo.smartcontract instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.neo.smartcontract",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.blockchain.unscoped.neo.smartcontract instead.")
        )
})
public interface NeoSmartContractService {

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
     * Fetches a specific {@link ElementsSmartContract} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param contractId the contract ID
     * @return the {@link ElementsSmartContract}, never null
     */
    ElementsSmartContract getNeoSmartContract(String contractId);

    /**
     * Updates the supplied {@link ElementsSmartContract}.  The
     * {@link PatchSmartContractRequest} method is used to key the
     * {@link ElementsSmartContract}.
     *
     * @param patchSmartContractRequest the {@link PatchSmartContractRequest} with the information to update
     * @return the {@link ElementsSmartContract} as it was changed by the service.
     */
    ElementsSmartContract patchNeoSmartContract(PatchSmartContractRequest patchSmartContractRequest);

    /**
     * Mints the token id's supplied in the {@link MintTokenRequest} using their linked {@link ElementsSmartContract}.
     *
     * @param mintTokenRequest  the {@link MintTokenRequest} containing the token id's and wallet with funds to mint.
     * @param exceptionConsumer
     * @return the {@link List<NeoSendRawTransaction>} responses from the blockchain.
     */
    PendingOperation mintToken(final MintTokenRequest mintTokenRequest,
                               final Consumer<MintNeoTokenResponse> applicationLogConsumer,
                               final Consumer<Throwable> exceptionConsumer);

    /**
     * Invokes a method on the {@link ElementsSmartContract} corresponding to the passed contract id
     * in a transactional manner. This will always incur a GAS fee.
     *
     * @param invokeRequest     the {@link InvokeContractRequest} with the information to invoke
     * @param exceptionConsumer
     * @return the {@link NeoSendRawTransaction} response from the blockchain invocation.
     */
    PendingOperation invoke(final InvokeContractRequest invokeRequest,
                            final BiConsumer<Long, InvokeContractResponse> applicationLogConsumer,
                            final Consumer<Throwable> exceptionConsumer);

    /**
     * Invokes a method on the {@link ElementsSmartContract} corresponding to the passed contract id.
     *
     * @param invokeRequest the {@link InvokeContractRequest} with the information to invoke
     * @return the {@link NeoSendRawTransaction} response from the blockchain invocation.
     */
    NeoInvokeFunction testInvoke(InvokeContractRequest invokeRequest);

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

package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.model.blockchain.contract.FlowInvokeContractResponse;
import org.mapstruct.Mapper;
import org.onflow.sdk.FlowTransactionResult;

/**
 * Maps the {@link FlowTransactionResult} returned by the Flow SDK (after the invocation transaction
 * reaches a terminal state) into the {@link FlowInvokeContractResponse} returned by the blockchain
 * invocation API. The Flow transaction status enum renders as its name.
 */
@Mapper
@ElementPrivate
public interface FlowInvokeContractResponseMapper {

    FlowInvokeContractResponse toFlowInvokeContractResponse(FlowTransactionResult transactionResult);

}

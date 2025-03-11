package dev.getelements.elements.sdk.model.blockchain.contract.near;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;

@Schema
public class NearInvokeContractResponse {

    @Schema(description = "status")
    private NearStatus status;
    @Schema(description = "transaction_outcome")
    private NearTransactionOutcome transactionOutcome;
    @Schema(description = "receipts_outcome")
    private List<NearReceiptOutcome> receiptsOutcome;


    public NearStatus getStatus() {
        return this.status;
    }

    public NearTransactionOutcome getTransactionOutcome() {
        return this.transactionOutcome;
    }
    
    public List<NearReceiptOutcome> getReceiptsOutcome() {
        return this.receiptsOutcome;
    }

    public void setStatus(NearStatus status) {
        this.status = status;
    }

    public void setTransactionOutcome(NearTransactionOutcome transactionOutcome) {
        this.transactionOutcome = transactionOutcome;
    }

    public void setReceiptsOutcome(List<NearReceiptOutcome> receiptsOutcome) {
        this.receiptsOutcome = receiptsOutcome;
    }

    public NearInvokeContractResponse() {
    }

    public NearInvokeContractResponse(NearStatus status, NearTransactionOutcome transactionOutcome, List<NearReceiptOutcome> receiptsOutcome) {
        this.status = status;
        this.transactionOutcome = transactionOutcome;
        this.receiptsOutcome = receiptsOutcome;
    }
}

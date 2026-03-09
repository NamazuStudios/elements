package dev.getelements.elements.sdk.model.blockchain.contract.near;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;

/** Represents the response from invoking a method on a NEAR smart contract. */
@Schema
public class NearInvokeContractResponse {

    @Schema(description = "status")
    private NearStatus status;
    @Schema(description = "transaction_outcome")
    private NearTransactionOutcome transactionOutcome;
    @Schema(description = "receipts_outcome")
    private List<NearReceiptOutcome> receiptsOutcome;

    /**
     * Returns the transaction status.
     *
     * @return the status
     */
    public NearStatus getStatus() {
        return this.status;
    }

    /**
     * Returns the transaction outcome.
     *
     * @return the transaction outcome
     */
    public NearTransactionOutcome getTransactionOutcome() {
        return this.transactionOutcome;
    }

    /**
     * Returns the list of receipt outcomes.
     *
     * @return the receipts outcome
     */
    public List<NearReceiptOutcome> getReceiptsOutcome() {
        return this.receiptsOutcome;
    }

    /**
     * Sets the transaction status.
     *
     * @param status the status
     */
    public void setStatus(NearStatus status) {
        this.status = status;
    }

    /**
     * Sets the transaction outcome.
     *
     * @param transactionOutcome the transaction outcome
     */
    public void setTransactionOutcome(NearTransactionOutcome transactionOutcome) {
        this.transactionOutcome = transactionOutcome;
    }

    /**
     * Sets the list of receipt outcomes.
     *
     * @param receiptsOutcome the receipts outcome
     */
    public void setReceiptsOutcome(List<NearReceiptOutcome> receiptsOutcome) {
        this.receiptsOutcome = receiptsOutcome;
    }

    /** Creates a new instance. */
    public NearInvokeContractResponse() {
    }

    /**
     * Creates a new instance with the given parameters.
     *
     * @param status the transaction status
     * @param transactionOutcome the transaction outcome
     * @param receiptsOutcome the list of receipt outcomes
     */
    public NearInvokeContractResponse(NearStatus status, NearTransactionOutcome transactionOutcome, List<NearReceiptOutcome> receiptsOutcome) {
        this.status = status;
        this.transactionOutcome = transactionOutcome;
        this.receiptsOutcome = receiptsOutcome;
    }
}

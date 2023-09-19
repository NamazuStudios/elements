package dev.getelements.elements.model.blockchain.contract.near;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel
public class NearInvokeContractResponse {

    @ApiModelProperty("status")
    private NearStatus status;
    @ApiModelProperty("transaction_outcome")
    private NearTransactionOutcome transactionOutcome;
    @ApiModelProperty("receipts_outcome")
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

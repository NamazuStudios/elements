package dev.getelements.elements.model.blockchain.contract.near;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class NearOutcome {
    @ApiModelProperty("logs")
    private List<String> logs;
    @ApiModelProperty("receipt_ids")
    private List<String> receiptIds;
    @ApiModelProperty("gas_burnt")
    private long gasBurnt;
    @ApiModelProperty("tokens_burnt")
    private String tokensBurnt;
    @ApiModelProperty("executor_id")
    private String executorId;
    @ApiModelProperty("status")
    private NearStatus status;
    @ApiModelProperty("metadata")
    private NearMetadata metadata;

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public List<String> getReceiptIds() {
        return receiptIds;
    }

    public void setReceiptIds(List<String> receiptIds) {
        this.receiptIds = receiptIds;
    }

    public long getGasBurnt() {
        return gasBurnt;
    }

    public void setGasBurnt(long gasBurnt) {
        this.gasBurnt = gasBurnt;
    }

    public String getTokensBurnt() {
        return tokensBurnt;
    }

    public void setTokensBurnt(String tokensBurnt) {
        this.tokensBurnt = tokensBurnt;
    }

    public String getExecutorId() {
        return executorId;
    }

    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }

    public NearStatus getStatus() {
        return status;
    }

    public void setStatus(NearStatus status) {
        this.status = status;
    }

    public NearMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(NearMetadata metadata) {
        this.metadata = metadata;
    }
}

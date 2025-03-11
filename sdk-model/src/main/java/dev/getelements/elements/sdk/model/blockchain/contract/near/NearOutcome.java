package dev.getelements.elements.sdk.model.blockchain.contract.near;



import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class NearOutcome {
    @Schema(description = "logs")
    private List<String> logs;
    @Schema(description = "receipt_ids")
    private List<String> receiptIds;
    @Schema(description = "gas_burnt")
    private long gasBurnt;
    @Schema(description = "tokens_burnt")
    private String tokensBurnt;
    @Schema(description = "executor_id")
    private String executorId;
    @Schema(description = "status")
    private NearStatus status;
    @Schema(description = "metadata")
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

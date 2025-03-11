package dev.getelements.elements.sdk.model.blockchain.contract.near;



import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;

public class NearContractFunctionCallResult {

    @Schema(description = "result")
    private byte[] result;

    @Schema(description = "logs")
    private Collection<String> logs;

    @Schema(description = "block_height")
    private long blockHeight;

    @Schema(description = "block_hash")
    private NearEncodedHash blockHash;

    @Schema(description = "error")
    private String error;

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    public Collection<String> getLogs() {
        return logs;
    }

    public void setLogs(Collection<String> logs) {
        this.logs = logs;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public NearEncodedHash getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(NearEncodedHash blockHash) {
        this.blockHash = blockHash;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}

package dev.getelements.elements.model.blockchain.contract.near;

import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;

public class NearContractFunctionCallResult {

    @ApiModelProperty("result")
    private byte[] result;

    @ApiModelProperty("logs")
    private Collection<String> logs;

    @ApiModelProperty("block_height")
    private long blockHeight;

    @ApiModelProperty("block_hash")
    private NearEncodedHash blockHash;

    @ApiModelProperty("error")
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

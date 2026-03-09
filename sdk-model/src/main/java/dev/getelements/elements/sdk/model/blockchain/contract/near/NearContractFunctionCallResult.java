package dev.getelements.elements.sdk.model.blockchain.contract.near;



import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;

/** Represents the result of calling a function on a NEAR smart contract. */
public class NearContractFunctionCallResult {

    /** Creates a new instance. */
    public NearContractFunctionCallResult() {}

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

    /**
     * Returns the raw result bytes.
     *
     * @return the result bytes
     */
    public byte[] getResult() {
        return result;
    }

    /**
     * Sets the raw result bytes.
     *
     * @param result the result bytes
     */
    public void setResult(byte[] result) {
        this.result = result;
    }

    /**
     * Returns the log messages from the contract call.
     *
     * @return the logs
     */
    public Collection<String> getLogs() {
        return logs;
    }

    /**
     * Sets the log messages from the contract call.
     *
     * @param logs the logs
     */
    public void setLogs(Collection<String> logs) {
        this.logs = logs;
    }

    /**
     * Returns the block height at which the call was executed.
     *
     * @return the block height
     */
    public long getBlockHeight() {
        return blockHeight;
    }

    /**
     * Sets the block height at which the call was executed.
     *
     * @param blockHeight the block height
     */
    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    /**
     * Returns the hash of the block at which the call was executed.
     *
     * @return the block hash
     */
    public NearEncodedHash getBlockHash() {
        return blockHash;
    }

    /**
     * Sets the hash of the block at which the call was executed.
     *
     * @param blockHash the block hash
     */
    public void setBlockHash(NearEncodedHash blockHash) {
        this.blockHash = blockHash;
    }

    /**
     * Returns the error message, if any.
     *
     * @return the error message, or null if none
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message.
     *
     * @param error the error message
     */
    public void setError(String error) {
        this.error = error;
    }

}

package dev.getelements.elements.sdk.model.blockchain.contract;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;
import java.util.Objects;

/** Represents the response from invoking a method on an EVM smart contract. */
@Schema
public class EVMInvokeContractResponse {

    /** Creates a new instance. */
    public EVMInvokeContractResponse() {}

    @Schema(description = "The transaction hash.")
    private String transactionHash;

    @Schema(description = "The transaction index.")
    private long transactionIndex;

    @Schema(description = "The block hash that contains the transaction.")
    private String blockHash;

    @Schema(description = "The block number that contains the transaction.")
    private long blockNumber;

    @Schema(description = "The cumulative gas used.")
    private long cumulativeGasUsed;

    @Schema(description = "The gas used.")
    private long gasUsed;

    @Schema(description = "The contract address.")
    private String contractAddress;

    @Schema(description = "The root.")
    private String root;

    // status is only present on Byzantium transactions onwards
    // see EIP 658 https://github.com/ethereum/EIPs/pull/658
    @Schema(description = "The status. Only present on Byzantium transactions onwards.")
    private long status;

    @Schema(description = "The address of the sender.")
    private String from;

    @Schema(description = "The address of the recipient.")
    private String to;

    @Schema(description = "The logs of any emitted events.")
    private List<EVMTransactionLog> logs;

    @Schema(description = "The logs bloom.")
    private String logsBloom;

    @Schema(description = "The revert reason, if any.")
    private String revertReason;

    @Schema(description = "The type.")
    private String type;

    @Schema(description = "The effective gas price.")
    private String effectiveGasPrice;

    @Schema(description = 
            "The decoded log. Elements will attempt to decode the first log using the passed in " +
            "output types. This is made available to get any sort of return description from an emitted event, " +
            "as transactions that affect state or storage on a contract cannot directly return values.")
    private List<Object> decodedLog;

    /**
     * Returns the transaction hash.
     *
     * @return the transaction hash
     */
    public String getTransactionHash() {
        return transactionHash;
    }

    /**
     * Sets the transaction hash.
     *
     * @param transactionHash the transaction hash
     */
    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    /**
     * Returns the transaction index.
     *
     * @return the transaction index
     */
    public long getTransactionIndex() {
        return transactionIndex;
    }

    /**
     * Returns the raw transaction index.
     *
     * @return the raw transaction index
     */
    public long getTransactionIndexRaw() {
        return transactionIndex;
    }

    /**
     * Sets the transaction index.
     *
     * @param transactionIndex the transaction index
     */
    public void setTransactionIndex(long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    /**
     * Returns the block hash.
     *
     * @return the block hash
     */
    public String getBlockHash() {
        return blockHash;
    }

    /**
     * Sets the block hash.
     *
     * @param blockHash the block hash
     */
    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    /**
     * Returns the block number.
     *
     * @return the block number
     */
    public long getBlockNumber() {
        return blockNumber;
    }

    /**
     * Sets the block number.
     *
     * @param blockNumber the block number
     */
    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * Returns the cumulative gas used.
     *
     * @return the cumulative gas used
     */
    public long getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    /**
     * Sets the cumulative gas used.
     *
     * @param cumulativeGasUsed the cumulative gas used
     */
    public void setCumulativeGasUsed(long cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    /**
     * Returns the gas used.
     *
     * @return the gas used
     */
    public long getGasUsed() {
        return gasUsed;
    }

    /**
     * Sets the gas used.
     *
     * @param gasUsed the gas used
     */
    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    /**
     * Returns the contract address.
     *
     * @return the contract address
     */
    public String getContractAddress() {
        return contractAddress;
    }

    /**
     * Sets the contract address.
     *
     * @param contractAddress the contract address
     */
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    /**
     * Returns the root.
     *
     * @return the root
     */
    public String getRoot() {
        return root;
    }

    /**
     * Sets the root.
     *
     * @param root the root
     */
    public void setRoot(String root) {
        this.root = root;
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public long getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status
     */
    public void setStatus(long status) {
        this.status = status;
    }

    /**
     * Returns true if the status indicates success (status == 1).
     *
     * @return true if status is OK
     */
    public boolean isStatusOK() {
        return getStatus() == 1;
    }

    /**
     * Returns the address of the sender.
     *
     * @return the from address
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the address of the sender.
     *
     * @param from the from address
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Returns the address of the recipient.
     *
     * @return the to address
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the address of the recipient.
     *
     * @param to the to address
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Returns the logs of any emitted events.
     *
     * @return the logs
     */
    public List<EVMTransactionLog> getLogs() {
        return logs;
    }

    /**
     * Sets the logs of any emitted events.
     *
     * @param logs the logs
     */
    public void setLogs(List<EVMTransactionLog> logs) {
        this.logs = logs;
    }

    /**
     * Returns the logs bloom.
     *
     * @return the logs bloom
     */
    public String getLogsBloom() {
        return logsBloom;
    }

    /**
     * Sets the logs bloom.
     *
     * @param logsBloom the logs bloom
     */
    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    /**
     * Returns the revert reason, if any.
     *
     * @return the revert reason
     */
    public String getRevertReason() {
        return revertReason;
    }

    /**
     * Sets the revert reason.
     *
     * @param revertReason the revert reason
     */
    public void setRevertReason(String revertReason) {
        this.revertReason = revertReason;
    }

    /**
     * Returns the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the effective gas price.
     *
     * @return the effective gas price
     */
    public String getEffectiveGasPrice() {
        return effectiveGasPrice;
    }

    /**
     * Sets the effective gas price.
     *
     * @param effectiveGasPrice the effective gas price
     */
    public void setEffectiveGasPrice(String effectiveGasPrice) {
        this.effectiveGasPrice = effectiveGasPrice;
    }

    /**
     * Returns the decoded log.
     *
     * @return the decoded log
     */
    public List<Object> getDecodedLog() {
        return decodedLog;
    }

    /**
     * Sets the decoded log.
     *
     * @param decodedLog the decoded log
     */
    public void setDecodedLog(List<Object> decodedLog) {
        this.decodedLog = decodedLog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EVMInvokeContractResponse that = (EVMInvokeContractResponse) o;
        return getTransactionIndex() == that.getTransactionIndex() && getBlockNumber() == that.getBlockNumber() && getCumulativeGasUsed() == that.getCumulativeGasUsed() && getGasUsed() == that.getGasUsed() && getStatus() == that.getStatus() && Objects.equals(getTransactionHash(), that.getTransactionHash()) && Objects.equals(getBlockHash(), that.getBlockHash()) && Objects.equals(getContractAddress(), that.getContractAddress()) && Objects.equals(getRoot(), that.getRoot()) && Objects.equals(getFrom(), that.getFrom()) && Objects.equals(getTo(), that.getTo()) && Objects.equals(getLogs(), that.getLogs()) && Objects.equals(getLogsBloom(), that.getLogsBloom()) && Objects.equals(getRevertReason(), that.getRevertReason()) && Objects.equals(getType(), that.getType()) && Objects.equals(getEffectiveGasPrice(), that.getEffectiveGasPrice()) && Objects.equals(getDecodedLog(), that.getDecodedLog());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTransactionHash(), getTransactionIndex(), getBlockHash(), getBlockNumber(), getCumulativeGasUsed(), getGasUsed(), getContractAddress(), getRoot(), getStatus(), getFrom(), getTo(), getLogs(), getLogsBloom(), getRevertReason(), getType(), getEffectiveGasPrice(), getDecodedLog());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EVMInvokeContractResponse{");
        sb.append("transactionHash='").append(transactionHash).append('\'');
        sb.append(", transactionIndex=").append(transactionIndex);
        sb.append(", blockHash='").append(blockHash).append('\'');
        sb.append(", blockNumber=").append(blockNumber);
        sb.append(", cumulativeGasUsed=").append(cumulativeGasUsed);
        sb.append(", gasUsed=").append(gasUsed);
        sb.append(", contractAddress='").append(contractAddress).append('\'');
        sb.append(", root='").append(root).append('\'');
        sb.append(", status=").append(status);
        sb.append(", from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", logs=").append(logs);
        sb.append(", logsBloom='").append(logsBloom).append('\'');
        sb.append(", revertReason='").append(revertReason).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", effectiveGasPrice='").append(effectiveGasPrice).append('\'');
        sb.append(", decodedLog=").append(decodedLog);
        sb.append('}');
        return sb.toString();
    }

}

package dev.getelements.elements.sdk.model.blockchain.contract;



import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

/** Represents an EVM transaction log entry. */
public class EVMTransactionLog {

    /** Creates a new instance. */
    public EVMTransactionLog() {}

    @Schema(description = "The removed status.")
    private boolean removed;

    @Schema(description = "The log index.")
    private long logIndex;

    @Schema(description = "The transaction index.")
    private long transactionIndex;

    @Schema(description = "The transation hash.")
    private String transactionHash;

    @Schema(description = "The block hash.")
    private String blockHash;

    @Schema(description = "The block number.")
    private long blockNumber;

    @Schema(description = "The address.")
    private String address;

    @Schema(description = "The encoded data.")
    private String data;

    @Schema(description = "The type of log.")
    private String type;

    @Schema(description = "The hash of any emitted events.")
    private List<String> topics;

    /**
     * Returns the removed status.
     *
     * @return the removed status
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Sets the removed status.
     *
     * @param removed the removed status
     */
    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    /**
     * Returns the log index.
     *
     * @return the log index
     */
    public long getLogIndex() {
        return logIndex;
    }

    /**
     * Sets the log index.
     *
     * @param logIndex the log index
     */
    public void setLogIndex(long logIndex) {
        this.logIndex = logIndex;
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
     * Sets the transaction index.
     *
     * @param transactionIndex the transaction index
     */
    public void setTransactionIndex(long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

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
     * Returns the address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address.
     *
     * @param address the address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the encoded data.
     *
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the encoded data.
     *
     * @param data the data
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Returns the type of log.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of log.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the hash of any emitted events.
     *
     * @return the topics
     */
    public List<String> getTopics() {
        return topics;
    }

    /**
     * Sets the hash of any emitted events.
     *
     * @param topics the topics
     */
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EVMTransactionLog that = (EVMTransactionLog) o;
        return isRemoved() == that.isRemoved() && getLogIndex() == that.getLogIndex() && getTransactionIndex() == that.getTransactionIndex() && getBlockNumber() == that.getBlockNumber() && Objects.equals(getTransactionHash(), that.getTransactionHash()) && Objects.equals(getBlockHash(), that.getBlockHash()) && Objects.equals(getAddress(), that.getAddress()) && Objects.equals(getData(), that.getData()) && Objects.equals(getType(), that.getType()) && Objects.equals(getTopics(), that.getTopics());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRemoved(), getLogIndex(), getTransactionIndex(), getTransactionHash(), getBlockHash(), getBlockNumber(), getAddress(), getData(), getType(), getTopics());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EVMTransactionLog{");
        sb.append("removed=").append(removed);
        sb.append(", logIndex=").append(logIndex);
        sb.append(", transactionIndex=").append(transactionIndex);
        sb.append(", transactionHash='").append(transactionHash).append('\'');
        sb.append(", blockHash='").append(blockHash).append('\'');
        sb.append(", blockNumber=").append(blockNumber);
        sb.append(", address='").append(address).append('\'');
        sb.append(", data='").append(data).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", topics=").append(topics);
        sb.append('}');
        return sb.toString();
    }

}

package dev.getelements.elements.model.blockchain.contract;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

public class EVMTransactionLog {

    @ApiModelProperty("The removed status.")
    private boolean removed;

    @ApiModelProperty("The log index.")
    private long logIndex;

    @ApiModelProperty("The transaction index.")
    private long transactionIndex;

    @ApiModelProperty("The transation hash.")
    private String transactionHash;

    @ApiModelProperty("The block hash.")
    private String blockHash;

    @ApiModelProperty("The block number.")
    private long blockNumber;

    @ApiModelProperty("The address.")
    private String address;

    @ApiModelProperty("The encoded data.")
    private String data;

    @ApiModelProperty("The type of log.")
    private String type;

    @ApiModelProperty("The hash of any emitted events.")
    private List<String> topics;

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public long getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(long logIndex) {
        this.logIndex = logIndex;
    }

    public long getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTopics() {
        return topics;
    }

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

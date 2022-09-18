package com.namazustudios.socialengine.model.blockchain;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class EVMInvokeContractResponse {

    @ApiModelProperty("The transaction hash.")
    private String transactionHash;

    @ApiModelProperty("The transaction index.")
    private long transactionIndex;

    @ApiModelProperty("The block hash that contains the transaction.")
    private String blockHash;

    @ApiModelProperty("The block number that contains the transaction.")
    private long blockNumber;

    @ApiModelProperty("The cumulative gas used.")
    private long cumulativeGasUsed;

    @ApiModelProperty("The gas used.")
    private long gasUsed;

    @ApiModelProperty("The contract address.")
    private String contractAddress;

    @ApiModelProperty("The root.")
    private String root;

    // status is only present on Byzantium transactions onwards
    // see EIP 658 https://github.com/ethereum/EIPs/pull/658
    @ApiModelProperty("The status. Only present on Byzantium transactions onwards.")
    private long status;

    @ApiModelProperty("The address of the sender.")
    private String from;

    @ApiModelProperty("The address of the recipient.")
    private String to;

    @ApiModelProperty("The logs of any emitted events.")
    private List<EVMTransactionLog> logs;

    @ApiModelProperty("The logs bloom.")
    private String logsBloom;

    @ApiModelProperty("The revert reason, if any.")
    private String revertReason;

    @ApiModelProperty("The type.")
    private String type;

    @ApiModelProperty("The effective gas price.")
    private String effectiveGasPrice;

    @ApiModelProperty("The decoded log. Elements will attempt to decode the first log using the passed in " +
            "output types. This is made available to get any sort of return value from an emitted event, " +
            "as transactions that affect state or storage on a contract cannot directly return values.")
    private List<Object> decodedLog;

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public long getTransactionIndex() {
        return transactionIndex;
    }

    public long getTransactionIndexRaw() {
        return transactionIndex;
    }

    public void setTransactionIndex(long transactionIndex) {
        this.transactionIndex = transactionIndex;
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

    public long getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(long cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public boolean isStatusOK() {
//        if (null == getStatus()) {
//            return true;
//        }

        return getStatus() == 1;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<EVMTransactionLog> getLogs() {
        return logs;
    }

    public void setLogs(List<EVMTransactionLog> logs) {
        this.logs = logs;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    public String getRevertReason() {
        return revertReason;
    }

    public void setRevertReason(String revertReason) {
        this.revertReason = revertReason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEffectiveGasPrice() {
        return effectiveGasPrice;
    }

    public void setEffectiveGasPrice(String effectiveGasPrice) {
        this.effectiveGasPrice = effectiveGasPrice;
    }

    public List<Object> getDecodedLog() {
        return decodedLog;
    }

    public void setDecodedLog(List<Object> decodedLog) {
        this.decodedLog = decodedLog;
    }

}
